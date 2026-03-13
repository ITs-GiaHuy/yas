
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
        stage('Security: Gitleaks Scan') {
            steps {
                script {
                    echo "Running Gitleaks to detect hardcoded secrets..."
                    catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                        sh 'docker run --rm -v $(pwd):/path zricethezav/gitleaks:latest detect --source="/path" -v'
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
                    stage('Test') {
                        when { 
                            anyOf {
                                changeset pattern: "${SERVICE}/**/*", comparator: 'GLOB'
                                changeset pattern: "common-library/**/*", comparator: 'GLOB'
                                changeset pattern: "pom.xml", comparator: 'GLOB'
                            }
                        }
                        steps {
                            // Run tests and generate coverage
                            sh "mvn clean test -pl ${SERVICE} -am"
                            
                            // Run SonarQube analysis
                            dir("${SERVICE}") {
                                withSonarQubeEnv('SonarCloud') {
                                    sh """
                                        mvn sonar:sonar \
                                        -Dsonar.projectKey=ITs-GiaHuy_yas \
                                        -Dsonar.organization=its-giahuy \
                                        -Dsonar.host.url=https://sonarcloud.io
                                    """
                                }
                            }
                            
                            // Run Snyk security scan
                            dir("${SERVICE}") {
                                withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                    sh "chmod +x mvnw || true"
                                    sh "npx snyk test --file=pom.xml --severity-threshold=high"
                                }
                            }
                        }
                        post {
                            always {
                                dir("${SERVICE}") {
                                    junit 'target/surefire-reports/*.xml'
                                    
                                    jacoco(
                                        execPattern: 'target/jacoco.exec',
                                        classPattern: 'target/classes',
                                        sourcePattern: 'src/main/java',
                                        inclusionPattern: '**/*.class',
                                        minimumLineCoverage: '70', 
                                        changeBuildStatus: true
                                    )
                                }
                            }
                        }
                    }
                    
                    stage('Build') {
                        when { 
                            anyOf {
                                changeset pattern: "${SERVICE}/**/*", comparator: 'GLOB'
                                changeset pattern: "common-library/**/*", comparator: 'GLOB'
                                changeset pattern: "pom.xml", comparator: 'GLOB'
                            }
                        }
                        steps {
                            // Build without tests
                            sh "mvn package -DskipTests -pl ${SERVICE} -am"
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
                    // Đã sửa lại thành chuỗi tĩnh cố định
                    stage('Node.js CI') {
                        when { 
                            changeset pattern: "${UI_SERVICE}/**/*", comparator: 'GLOB'
                        }
                        steps {
                            dir("${UI_SERVICE}") {
                                echo "Building UI/BFF Project: ${UI_SERVICE}..."
                                sh 'npm ci'
                                sh 'npm run lint'
                                sh 'npm run test --if-present'
                                sh 'npm run build'

                                withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                    sh 'npx snyk test'
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}