// pipeline {
//     agent any
    
//     tools {
//         jdk 'JDK-21'
//         maven 'maven-3'
//         nodejs 'NodeJS-20' 
//     }

//     options {
//         buildDiscarder(logRotator(numToKeepStr: '10'))
//         disableConcurrentBuilds()
//     }

//     stages {
//         stage('Setup Tools') {
//             steps {
//                 script {
//                     // [Fix 2.1] Chỉ tải Snyk CLI nếu chưa tồn tại trong workspace
//                     if (!fileExists('./snyk')) {
//                         echo "Downloading Snyk CLI standalone binary..."
//                         sh '''
//                             curl -s -Lo ./snyk https://github.com/snyk/snyk/releases/latest/download/snyk-linux
//                             chmod +x ./snyk
//                         '''
//                     } else {
//                         echo "Snyk CLI already exists. Skipping download."
//                     }
                    
//                     // [Fix 3] Xác thực Snyk 1 lần duy nhất ở đầu pipeline
//                     withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
//                         sh './snyk auth $SNYK_TOKEN'
//                     }
//                 }
//             }
//         }

//         stage('Gitleaks Scan') {
//             steps {
//                 script {
//                     echo "Running Gitleaks to detect hardcoded secrets..."
//                     catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
//                         // [Fix 5] Bỏ --no-git để Gitleaks quét cả lịch sử commit
//                         sh 'docker run --rm -v "${WORKSPACE}:/work" -w /work zricethezav/gitleaks:latest detect --source="." --verbose'
//                     }
//                 }
//             }
//         }

//         stage('Pre-build Java Dependencies') {
//             when { 
//                 anyOf {
//                     changeset pattern: "**/*.java", comparator: 'GLOB'
//                     changeset pattern: "**/pom.xml", comparator: 'GLOB'
//                 }
//             }
//             steps {
//                 sh "mvn clean install -DskipTests" 
//             }
//         }

//         stage('Backend Services CI (Java)') {
//             matrix {
//                 axes {
//                     axis {
//                         name 'SERVICE'
//                         // [Fix 1] Đã gỡ bỏ 'backoffice-bff' và 'storefront-bff' khỏi Java Matrix
//                         values 'media', 'product', 'cart', 'order', 'payment', 
//                                'search', 'customer', 'inventory', 'delivery', 
//                                'location', 'promotion', 'rating', 
//                                'recommendation', 'tax', 'webhook', 'storefront-bff', 'backoffice-bff'
//                     }
//                 }
//                 stages {
//                     stage('Java CI - ${SERVICE}') {
//                         when { 
//                             anyOf {
//                                 changeset pattern: "${SERVICE}/**/*", comparator: 'GLOB'
//                                 changeset pattern: "common-library/**/*", comparator: 'GLOB'
//                                 changeset pattern: "pom.xml", comparator: 'GLOB'
//                             }
//                         }
//                         steps {
//                             sh "mvn verify -pl ${SERVICE}"
                            
//                             // [Fix 3] Bỏ || true để Snyk thực sự block build nếu có lỗi HIGH
//                             sh "./snyk test --file=${SERVICE}/pom.xml --severity-threshold=high"

//                             // [Fix 4] Chạy SonarQube từ root directory, chỉ định project list (-pl)
//                             withSonarQubeEnv('SonarCloud') {
//                                 sh """
//                                     mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
//                                     -pl ${SERVICE} \
//                                     -Dsonar.projectKey=ITs-GiaHuy_yas_${SERVICE} \
//                                     -Dsonar.projectName="Yas - ${SERVICE}" \
//                                     -Dsonar.organization=its-giahuy \
//                                     -Dsonar.host.url=https://sonarcloud.io
//                                 """
//                             }
//                         }
//                         post {
//                             always {
//                                 junit testResults: "${SERVICE}/target/surefire-reports/*.xml", allowEmptyResults: true
                                
//                                 jacoco(
//                                     execPattern: "${SERVICE}/target/**/jacoco.exec",
//                                     classPattern: "${SERVICE}/target/classes",
//                                     sourcePattern: "${SERVICE}/src/main/java",
//                                     inclusionPattern: '**/*.class',
//                                     maximumLineCoverage: '70',
//                                     minimumLineCoverage: '70',
//                                     changeBuildStatus: true 
//                                 )
//                             }
//                         }
//                     }
//                 }
//             }
//         }

//         stage('Frontend & BFF CI (Node.js)') {
//             matrix {
//                 axes {
//                     axis {
//                         name 'UI_SERVICE'
//                         values 'storefront', 'backoffice'
//                     }
//                 }
//                 stages {
//                     stage('Node.js CI Workflow') {
//                         when { 
//                             changeset pattern: "${UI_SERVICE}/**/*", comparator: 'GLOB'
//                         }
//                         steps {
//                             dir("${UI_SERVICE}") {
//                                 echo "Building UI/BFF Project: ${UI_SERVICE}..."
//                                 sh 'npm ci'
//                                 sh 'npm run lint'
//                                 sh 'npm run build'
//                             }
                            
//                             // [Fix 3] Bỏ || true để đảm bảo chặn mã độc/lỗ hổng
//                             sh "./snyk test --file=${UI_SERVICE}/package.json --severity-threshold=high"
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
        stage('Setup Tools') {
            steps {
                script {
                    echo "Checking if Snyk CLI is installed..."
                    // Kiểm tra xem lệnh snyk đã tồn tại trong PATH chưa
                    def snykExists = sh(script: 'command -v snyk', returnStatus: true) == 0
                    
                    if (!snykExists) {
                        echo "Snyk CLI not found. Installing via npm..."
                        sh 'npm install -g snyk'
                    } else {
                        echo "Snyk CLI is already installed. Skipping installation."
                    }
                }
            }
        }

        stage('Gitleaks Scan') {
            steps {
                script {
                    echo "Running Gitleaks to detect hardcoded secrets..."
                    catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                        sh 'docker run --rm -v "${WORKSPACE}:/work" -w /work zricethezav/gitleaks:latest detect --source="." --verbose'
                    }
                }
            }
        }

        stage('Pre-build Java Dependencies') {
            when { 
                anyOf {
                    changeset pattern: "**/*.java", comparator: 'GLOB'
                    changeset pattern: "**/pom.xml", comparator: 'GLOB'
                }
            }
            steps {
                sh "mvn clean install -DskipTests" 
            }
        }

        stage('Backend Services CI (Java)') {
            matrix {
                axes {
                    axis {
                        name 'SERVICE'
                        // Đã giữ nguyên danh sách service theo đúng ý bạn
                        values 'media', 'product', 'cart', 'order', 'payment', 
                               'search', 'customer', 'inventory', 'delivery', 
                               'location', 'promotion', 'rating', 
                               'recommendation', 'tax', 'webhook', 'storefront-bff', 'backoffice-bff'
                    }
                }
                stages {
                    stage('Build& Scan') {
                        when { 
                            anyOf {
                                changeset pattern: "${SERVICE}/**/*", comparator: 'GLOB'
                                changeset pattern: "common-library/**/*", comparator: 'GLOB'
                                changeset pattern: "pom.xml", comparator: 'GLOB'
                            }
                        }
                        steps {
                            // 1. Build & Test
                            sh "mvn verify -pl ${SERVICE}"
                            
                            // 2. Snyk Scan (Truyền token trực tiếp qua biến môi trường)
                            withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                sh "SNYK_TOKEN=\$SNYK_TOKEN snyk test --file=${SERVICE}/pom.xml --severity-threshold=high"
                            }

                            // 3. SonarQube Scan (Chạy bên trong thư mục con)
                            withSonarQubeEnv('SonarCloud') {
                                dir("${SERVICE}") {
                                    sh """
                                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                                        -Dsonar.projectKey=ITs-GiaHuy_yas_${SERVICE} \
                                        -Dsonar.projectName="Yas - ${SERVICE}" \
                                        -Dsonar.organization=its-giahuy \
                                        -Dsonar.host.url=https://sonarcloud.io
                                    """
                                }
                            }
                        }
                        post {
                            always {
                                // 4. JUnit Test Results
                                junit testResults: "${SERVICE}/target/surefire-reports/*.xml", allowEmptyResults: true
                                
                                // 5. JaCoCo Coverage Report
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
                        // Đã giữ nguyên danh sách service Frontend theo đúng ý bạn
                        values 'storefront', 'backoffice'
                    }
                }
                stages {
                    stage('Node.js') {
                        when { 
                            changeset pattern: "${UI_SERVICE}/**/*", comparator: 'GLOB'
                        }
                        steps {
                            dir("${UI_SERVICE}") {
                                echo "Building UI/BFF Project: ${UI_SERVICE}..."
                                sh 'npm ci'
                                sh 'npm run lint'
                                sh 'npm run build'
                            }
                            
                            withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                                sh "SNYK_TOKEN=\$SNYK_TOKEN snyk test --file=${UI_SERVICE}/package.json --severity-threshold=high"
                            }
                        }
                    }
                }
            }
        }
    }
}