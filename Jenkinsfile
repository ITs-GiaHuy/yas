pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'
        maven 'maven-3'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
        REVISION = '1.0-SNAPSHOT'
    }

    stages {
        stage('Security: Gitleaks Scan') {
            steps {
                script {
                    echo "Running Gitleaks to detect hardcoded secrets..."
                    sh 'docker run --rm -v $(pwd):/path zricethezav/gitleaks:latest detect --source="/path" -v || true'
                }
            }
        }

        stage('Install Root POM') {
            steps {
                echo "Installing Root POM to local repository..."
                // Thêm cờ -U để force update, xóa cache lỗi cũ
                sh "mvn clean install -N -U -Drevision=${REVISION}" 
            }
        }

        stage('Build Common Library') {
            steps {
                // Không dùng dir('common-library') nữa, chạy từ ROOT bằng -pl
                echo "Building and installing common-library to local Maven repo..."
                sh "mvn clean install -pl common-library -am -DskipTests -U -Drevision=${REVISION}"
            }
        }

        stage('Monorepo Services CI') {
            matrix {
                axes {
                    axis {
                        name 'SERVICE'
                        values 'media', 'product', 'cart', 'order', 'payment', 
                               'search', 'storefront', 'storefront-bff', 'backoffice', 
                               'backoffice-bff', 'customer', 'inventory', 'delivery', 
                               'identity', 'location', 'promotion', 'rating', 
                               'recommendation', 'sampledata', 'tax', 'webhook'
                    }
                }
                
                stages {
                    stage('Service CI') {
                        when { 
                            changeset "${SERVICE}/**" 
                        }
                        stages {
                            stage('Test & Coverage') {
                                steps {
                                    // Chạy từ ROOT, dùng -pl để build riêng service đó
                                    sh "mvn clean test jacoco:report -pl ${SERVICE} -am -U -Drevision=${REVISION}"
                                }
                                post {
                                    always {
                                        // Vẫn giữ dir() ở đây để Jenkins tìm đúng đường dẫn file report
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
                            
                            stage('Code Quality & SAST') {
                                steps {
                                    withSonarQubeEnv('SonarQube-Server') {
                                        // Chạy từ ROOT
                                        sh "mvn sonar:sonar -pl ${SERVICE} -am -U -Drevision=${REVISION}"
                                    }
                                }
                            }

                            stage('Build') {
                                steps {
                                    // Chạy từ ROOT
                                    sh "mvn package -DskipTests -pl ${SERVICE} -am -U -Drevision=${REVISION}"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}