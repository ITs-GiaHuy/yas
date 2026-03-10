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
                sh "mvn clean install -N -U -Drevision=${REVISION}" 
            }
        }

        stage('Build Common Library') {
            steps {
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
                                    sh "mvn clean test jacoco:report -pl ${SERVICE} -am -U -Drevision=${REVISION}"
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
                            
                            stage('Code Quality & SAST') {
                                steps {
                                    withSonarQubeEnv('SonarQube-Server') {
                                        sh "mvn sonar:sonar -pl ${SERVICE} -am -U -Drevision=${REVISION}"
                                    }
                                }
                            }

                            stage('Build') {
                                steps {
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