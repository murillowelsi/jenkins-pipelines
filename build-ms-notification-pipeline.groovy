pipeline {
    agent {
        docker {
            image 'maven'
            args '-v /root/.m2:/root/.m2'
        }
    }
    stages {
        stage('Checkout') {
            steps {
                git credentialsId: 'aws', url: 'https://git-codecommit.sa-east-1.amazonaws.com/v1/repos/ms-notification'
            }
        }        
        stage('Build, Release, Unit tests') {
            steps {
                // dir(path: 'ms-notification') {
                    sh 'mvn clean package'
                // }
            }   
        }
        stage('Archival') {
            steps {
                dir(path: '.') {
                    publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'target/site/jacoco/', reportFiles: 'index.html', reportName: 'Code Coverage', reportTitles: 'Code Coverage'])
                    step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/TEST-*.xml'])
                    archiveArtifacts "target/*.jar"
                }
            }   
        }
        stage('BDD tests job'){
            steps {
                build job: 'bdd-ms-notification-pipeline', wait: true
            }
        }    
    }        
}