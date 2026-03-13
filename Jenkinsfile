pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'     // Đảm bảo Jenkins đã cài JDK 21 
        maven 'maven-3'  
        nodejs 'NodeJS-20' // Đảm bảo Jenkins có NodeJS 20 [cite: 11]
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Security: Gitleaks Scan') {
            steps {
                script {
                    echo "Running Gitleaks to detect hardcoded secrets..."
                    // Tải image gitleaks giống luồng CI gốc [cite: 61]
                    sh 'docker pull zricethezav/gitleaks:v8.18.4'
                    
                    catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                        // Sử dụng ${WORKSPACE} thay vì $(pwd) để tránh lỗi path
                        sh 'docker run --rm -v "${WORKSPACE}:/work" -w /work zricethezav/gitleaks:v8.18.4 detect --source="." --no-git --verbose'
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
                            
                            // 2. Upload Snyk Scan (Chạy từ root, trỏ vào pom.xml của service)
                            withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                // Thêm -y để bypass prompt npx, ignore lỗi gãy build ngay lập tức nếu chỉ muốn report
                                sh "npx -y snyk test --file=${SERVICE}/pom.xml --severity-threshold=high"
                            }

                            // 3. SonarQube Scan (Chạy từ root dùng -pl) [cite: 25]
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
                                // 4. Test Results [cite: 21, 22]
                                junit testResults: "${SERVICE}/target/surefire-reports/*.xml", allowEmptyResults: true
                                
                                // 5. JaCoCo Coverage (Ép điều kiện > 70%)
                                jacoco(
                                    execPattern: "${SERVICE}/target/jacoco.exec",
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
                                sh 'npm run build' // [cite: 179]
                                
                                // Có thể có hoặc không có script test tuỳ service
                                catchError(buildResult: 'SUCCESS', stageResult: 'SUCCESS') {
                                    sh 'npm run test' 
                                }

                                withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                    sh 'npx -y snyk test --severity-threshold=high'
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}