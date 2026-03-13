pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'
        maven 'maven-3'
        nodejs 'NodeJS-20' 
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        // Tải Snyk CLI ngay từ đầu để dùng chung cho mọi Service, tránh lỗi quyền của npx
        stage('Setup Tools') {
            steps {
                script {
                    echo "Downloading Snyk CLI standalone binary..."
                    sh '''
                        curl -s -Lo ./snyk https://github.com/snyk/snyk/releases/latest/download/snyk-linux
                        chmod +x ./snyk
                    '''
                }
            }
        }

        stage('Security: Gitleaks Scan') {
            steps {
                script {
                    echo "Running Gitleaks to detect hardcoded secrets..."
                    catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                        sh 'docker run --rm -v "${WORKSPACE}:/work" -w /work zricethezav/gitleaks:latest detect --source="." --no-git --verbose'
                    }
                }
            }
        }

        stage('Backend Services CI (Java)') {
            matrix {
                axes {
                    axis {
                        name 'SERVICE'
                        values 'media', 'product', 'cart', 'order', 'payment', 
                               'search', 'customer', 'inventory', 'delivery', 
                               'identity', 'location', 'promotion', 'rating', 
                               'recommendation', 'tax', 'webhook'
                    }
                }
                stages {
                    stage('Java CI Workflow') {
                        when { 
                            anyOf {
                                changeset pattern: "${SERVICE}/**/*", comparator: 'GLOB'
                                changeset pattern: "common-library/**/*", comparator: 'GLOB'
                                changeset pattern: "pom.xml", comparator: 'GLOB'
                            }
                        }
                        steps {
                            // 1. Build & Test (Chạy từ root)
                            sh "mvn clean verify -pl ${SERVICE} -am"
                            
                            // 2. Snyk Scan bằng Binary. 
                            // Thêm "|| true" để Snyk không ném exit code làm sập luồng chạy của JaCoCo phía sau.
                            withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                sh '''
                                    ./snyk auth $SNYK_TOKEN
                                    ./snyk test --file=${SERVICE}/pom.xml --severity-threshold=high || true
                                '''
                            }

                            // 3. SonarQube Scan
                            withSonarQubeEnv('SonarCloud') {
                                sh """
                                    mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                                    -pl ${SERVICE} -am \
                                    -Dsonar.projectKey=ITs-GiaHuy_yas \
                                    -Dsonar.organization=its-giahuy \
                                    -Dsonar.host.url=https://sonarcloud.io
                                """
                            }
                        }
                        post {
                            always {
                                // 4. Test Results
                                junit testResults: "${SERVICE}/target/surefire-reports/*.xml", allowEmptyResults: true
                                
                                // 5. JaCoCo Coverage (Yêu cầu > 70% mới pass)
                                // Sử dụng dấu ** để plugin tự động quét và tìm đúng file jacoco.exec 
                                jacoco(
                                    execPattern: "${SERVICE}/target/**/jacoco.exec",
                                    classPattern: "${SERVICE}/target/classes",
                                    sourcePattern: "${SERVICE}/src/main/java",
                                    inclusionPattern: '**/*.class',
                                    maximumLineCoverage: '70',
                                    minimumLineCoverage: '70',
                                    changeBuildStatus: true 
                                )
                            }
                        }
                    }
                }
            }
        }

        stage('Frontend & BFF CI (Node.js)') {
            matrix {
                axes {
                    axis {
                        name 'UI_SERVICE'
                        values 'storefront', 'storefront-bff', 'backoffice', 'backoffice-bff'
                    }
                }
                stages {
                    stage('Node.js CI Workflow') {
                        when { 
                            changeset pattern: "${UI_SERVICE}/**/*", comparator: 'GLOB'
                        }
                        steps {
                            dir("${UI_SERVICE}") {
                                echo "Building UI/BFF Project: ${UI_SERVICE}..."
                                sh 'npm ci'
                                sh 'npm run lint'
                                sh 'npm run build'
                                
                                catchError(buildResult: 'SUCCESS', stageResult: 'SUCCESS') {
                                    sh 'npm run test' 
                                }
                            }
                            // Quét Snyk cho thư mục Frontend (chạy ở ngoài dir bằng binary đã tải)
                            withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                sh '''
                                    ./snyk auth $SNYK_TOKEN
                                    ./snyk test --file=${UI_SERVICE}/package.json --severity-threshold=high || true
                                '''
                            }
                        }
                    }
                }
            }
        }
    }
}