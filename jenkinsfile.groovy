#!groovy
pipeline {
  agent { label 'pod-tf' } //template pod utilizada para realizar a instalacao

  environment {
    FOLDER="spinnaker-for-gcp" // Adicionar a pasta do projeto que tenha tf
    SERVICE_ACCOUNT="sa-lab-spinnaker" // Adicionar IDs da credentials criada e adicionada no jenkins.
    PROJECT="labs" //Adicionar projeto
    ZONE="us-central1" // Zona do projeto
  }

  stages {
    stage('Atualizando package') {
      steps {
        container("gcloud") {
          dir(env.FOLDER) {
              sh "env"
              sh 'apk upgrade'
              sh 'apk add jq'
              sh 'apk add bash'
              sh 'apk add ncurses'
              sh 'apk add gettext'
          }
        }
      }
    }

    stage('Ativando conta e conectando no gke') {
       steps {
         container('gcloud') {
            withCredentials([file(credentialsId: env.SERVICE_ACCOUNT, variable: 'GOOGLE_CREDENTIALS')]) {
              dir(env.FOLDER) {
                sh "gcloud auth activate-service-account  --key-file=${GOOGLE_CREDENTIALS} --project=${PROJECT}" //autenticacao ao projeto do cluster
                sh "gcloud container clusters get-credentials spinnaker-install --region us-central1 --project labs" //autenticacao no cluster
                sh 'pwd'
                sh 'ls -la'        
              }
            }
         }  
      }
    }

    stage('Approval') {
      steps {
        googlechatnotification message: 'Esperando a sua aprovacao: ${JOB_NAME}', url: 'https://chat.googleapis.com/v1/spaces/AAAAzjFY7Q4/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=7h7e4F05CphBjvIRidtpR429hrmJvIHreMVn_WVq6cE%3D'        
        timeout(time: 360, unit: 'SECONDS') { 
          script {
            def userInput = input(id: 'confirm', message: 'Deploy Spinnaker?', parameters: [ [$class: 'BooleanParameterDefinition', defaultValue: false, description: 'Deploy Spinnaker', name: 'confirm'] ])
          }
        }
      }
    }
    
    stage('Deploy-spinnaker') {
      steps {
        container('gcloud') {
          withCredentials([file(credentialsId: env.SERVICE_ACCOUNT, variable: 'GOOGLE_CREDENTIALS')]) {
            dir('spinnaker-for-gcp') {
              sh 'pwd'
              sh 'ls -la'
              sh 'kubectl create ns halyard'
              sh 'sleep 5'
              sh 'kubectl create ns spinnaker'
              sh 'kubectl get ns'
              sh './scripts/install/setup.sh' //instalacao do spinnaker
              sh 'sleep 5'
              sh 'echo Criando NodePort spin-gate e spin-deck para ingress'
              sh 'ls -la'
              sh './NodePort.sh'// Criando services nodePort para ingress
            }
          }
        }
      }
    }
  }

  post {
    success {
      googlechatnotification message: 'Sucesso na pipeline: ${JOB_NAME}', url: 'https://chat.googleapis.com/v1/spaces/AAAAzjFY7Q4/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=7h7e4F05CphBjvIRidtpR429hrmJvIHreMVn_WVq6cE%3D'
    }
    failure {
      googlechatnotification message: 'Houve falha na pipeline: ${JOB_NAME}', url: 'https://chat.googleapis.com/v1/spaces/AAAAzjFY7Q4/messages?key=AIzaSyDdI0hCZtE6vySjMm-WEfRq3CPzqKqqsHI&token=7h7e4F05CphBjvIRidtpR429hrmJvIHreMVn_WVq6cE%3D'
    }
  }
}
