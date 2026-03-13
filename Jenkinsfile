// pipeline {
//     agent any
    
//     tools {
//         jdk 'JDK-21'
//         maven 'maven-3'
//     }

//     options {
//         buildDiscarder(logRotator(numToKeepStr: '10'))
//         disableConcurrentBuilds()
//     }

//     stages {
//         stage('Security: Gitleaks Scan') {
//             steps {
//                 script {
//                     echo "Running Gitleaks to detect hardcoded secrets..."
//                     sh 'docker run --rm -v $(pwd):/path zricethezav/gitleaks:latest detect --source="/path" -v || true'
//                 }
//             }
//         }

//         stage('Install Root POM') {
//             steps {
//                 echo "Installing Root POM to local repository..."
//                 sh 'mvn clean install -N' 
//             }
//         }

//         stage('Build Common Library') {
//             steps {
                
//                 dir('common-library') { 
//                     echo "Building and installing common-library to local Maven repo..."
//                     sh 'mvn clean install -DskipTests'
//                 }
//             }
//         }

//         stage('Monorepo Services CI') {
//             matrix {
//                 axes {
//                     axis {
//                         name 'SERVICE'
//                         values 'media', 'product', 'cart', 'order', 'payment', 
//                                'search', 'storefront', 'storefront-bff', 'backoffice', 
//                                'backoffice-bff', 'customer', 'inventory', 'delivery', 
//                                'identity', 'location', 'promotion', 'rating', 
//                                'recommendation', 'sampledata', 'tax', 'webhook'
//                     }
//                 }
                
//                 stages {
//                     stage('Service CI') {
//                         when { 
//                             changeset "${SERVICE}/**" 
//                         }
//                         stages {
//                             stage('Test & Coverage') {
//                                 steps {
//                                     dir("${SERVICE}") {
//                                         sh 'mvn clean test jacoco:report'
//                                     }
//                                 }
//                                 post {
//                                     always {
//                                         dir("${SERVICE}") {
//                                             junit 'target/surefire-reports/*.xml'

//                                             jacoco(
//                                                 execPattern: 'target/jacoco.exec',
//                                                 classPattern: 'target/classes',
//                                                 sourcePattern: 'src/main/java',
//                                                 inclusionPattern: '**/*.class',
//                                                 minimumLineCoverage: '70', 
//                                                 changeBuildStatus: true
//                                             )
//                                         }
//                                     }
//                                 }
//                             }
                            
//                             stage('Code Quality & SAST') {
//                                 steps {
//                                     dir("${SERVICE}") {
//                                         withSonarQubeEnv('SonarQube-Server') {
//                                             sh 'mvn sonar:sonar'
//                                         }
//                                     }
//                                 }
//                             }

//                             stage('Build') {
//                                 steps {
//                                     dir("${SERVICE}") {
//                                         sh 'mvn package -DskipTests'
//                                     }
//                                 }
//                             }
//                         }
//                     }
//                 }
//             }
//         }
//     }
// }

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
                    // Đã bỏ '|| true' để pipeline thực sự fail nếu phát hiện lộ credential trong code
                    sh 'docker run --rm -v $(pwd):/path zricethezav/gitleaks:latest detect --source="/path" -v'
                }
            }
        }

        stage('Install Root POM') {
            steps {
                echo "Installing Root POM to local repository..."
                sh 'mvn clean install -N' 
            }
        }

        stage('Build Common Library') {
            steps {
                dir('common-library') { 
                    echo "Building and installing common-library to local Maven repo..."
                    sh 'mvn clean install -DskipTests'
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
                    stage('Java CI') {
                        when { 
                            anyOf {
                                changeset "${SERVICE}/**"
                                changeset "common-library/**"
                                changeset "pom.xml"
                            }
                        }
                        stages {
                            stage('Build, Test, SAST & Coverage') {
                                steps {
                                    dir("${SERVICE}") {
                                        // Chạy SonarQube & Test
                                        withSonarQubeEnv('SonarCloud') {
                                            // Thay YOUR_PROJECT_KEY và YOUR_ORG_KEY bằng mã thực tế của nhóm bạn trên SonarCloud
                                            sh """
                                                mvn clean verify sonar:sonar \
                                                -Dsonar.projectKey=ITs-GiaHuy_yas \
                                                -Dsonar.organization=its-giahuy \
                                                -Dsonar.host.url=https://sonarcloud.io
                                            """
                                        }
                                        
                                        // BỔ SUNG: Chạy Snyk để scan lỗ hổng thư viện (Yêu cầu 7c)
                                        // Lưu ý: Cần cấu hình credential 'snyk-token' trên Jenkins
                                        withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                            sh 'npx snyk test --all-projects --severity-threshold=high'
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
                    stage('Node.js CI') {
                        when { 
                            changeset "${UI_SERVICE}/**" 
                        }
                        stages {
                            stage('Install, Lint, Test, Scan & Build') {
                                steps {
                                    dir("${UI_SERVICE}") {
                                        echo "Building UI/BFF Project: ${UI_SERVICE}..."
                                        sh 'npm ci'
                                        sh 'npm run lint'
                                        
                                        // Đã mở comment để chạy test và build cho Frontend (Yêu cầu 5)
                                        sh 'npm run test'
                                        sh 'npm run build'

                                        // BỔ SUNG: Chạy Snyk để scan package.json của Frontend
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
    }
}
