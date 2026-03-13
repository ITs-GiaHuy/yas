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
                    // Đã sửa lại thành chuỗi tĩnh cố định (bắt buộc trong Declarative Pipeline)
                    stage('Java CI') {
                        when { 
                            anyOf {
                                changeset pattern: "${SERVICE}/**/*", comparator: 'GLOB'
                                changeset pattern: "common-library/**/*", comparator: 'GLOB'
                                changeset pattern: "pom.xml", comparator: 'GLOB'
                            }
                        }
                        steps {
                            // BƯỚC 1: Dùng "install" để lưu Parent POM và common-library vào ~/.m2 cache cho Snyk sử dụng
                            sh "mvn clean install -pl ${SERVICE} -am"
                            
                            // BƯỚC 2: Di chuyển vào trong thư mục service để quét SonarQube
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
                            
                            // dir("${SERVICE}") {
                            //     withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                            //         // Đảm bảo quyền thực thi cho mvnw trong thư mục hiện tại
                            //         sh "chmod +x mvnw || true"
                                    
                            //         // Chạy Snyk và truyền trực tiếp -Drevision xuống Maven
                            //         sh "npx snyk test --file=pom.xml --severity-threshold=high -- -Drevision=1.0-SNAPSHOT"
                            //     }
                            // }
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