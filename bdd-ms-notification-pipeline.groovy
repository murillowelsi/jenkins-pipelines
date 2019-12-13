pipeline {
    agent {
        docker {
            image 'maven'
            args '-v /root/.m2:/root/.m2'
        }
    }
    stages {
        stage('checkout-test-repo') {
            steps {
                git credentialsId: 'f48509a6-ccb6-4e34-8e9a-faf3b20fa8da', url: 'https://murillopereira@bitbucket.org/abenderd/ultragaz.git'
            }
        }
        stage('Automated Tests, Cucumber Reports') {
            steps {
                sh 'mvn clean install -Dcucumber.options="--tags @POST_sms"'
                cucumber failedFeaturesNumber: -1, failedScenariosNumber: -1, failedStepsNumber: -1, fileIncludePattern: 'target/*.json', pendingStepsNumber: -1, skippedStepsNumber: -1, sortingMethod: 'ALPHABETICAL', undefinedStepsNumber: -1                       
            }
        }    
    }
}