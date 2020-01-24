# Jenkins - Declarative Pipelines

Existem diferentes maneiras de se criar pipelines no Jenkins. Os **pipelines declarativos** fornecem uma sintaxe simplificada mais amigável e com instruções específicas.

Os pipelines declarativos devem ser colocados dentro de um bloco de pipeline, por exemplo:

```
pipeline {
    /* colocar seu Declarative Pipeline aqui */
}
```

Dentro deste bloco devemos incluir as próximas seções necessárias, como:

 - agent
 - stages
 - stage
 - steps

## Agent

A seção do **agent** especifica onde o Pipeline inteiro, ou um estágio específico, será executado no ambiente Jenkins. Em nosso exemplo utilizaremos o docker como agente para executar nosso build.

O parâmetro **image**  faz download de uma imagem Docker do **maven: 3-alpine** (caso não exista na sua máquina), e executa essa imagem como um contêiner separado. 

O contêiner Maven se torna o agente que Jenkins usa para executar seu projeto de Pipeline. No entanto, esse contêiner tem vida curta, ou seja, é criado durante o início e depois é removido no final do seu pipeline.

O parâmetro args cria um mapeamento entre os diretórios /root/.m2 (repositório Maven) no contêiner **Maven Docker** e o do sistema de arquivos do **host**. 

```
pipeline {
	agent {
		docker {
			image 'maven'
			args '-v /root/.m2:/root/.m2'
		}
	}
}
```

## Stages

A seção Stages permite gerar diferentes estágios em seu pipeline que serão visualizados como segmentos diferentes quando a tarefa for executada.

```
pipeline {
	agent {
		docker {
			image 'maven'
			args '-v /root/.m2:/root/.m2'
		}
	}
	stages {
		/* colocar os estágios do seu pipeline aqui */
	}
}
```

## Stage e Steps

Pelo menos uma seção **stage** deve ser definida na seção **stages**. Dentro desse estágio estarão as instruções que o pipeline executará. Os estágios devem ser nomeados, pois o Jenkins exibirá cada um deles em **Stage View**.

A seção **steps** está contida dentro do bloco **stage**, e define uma série de uma ou mais etapas a serem executadas em uma determinada diretiva de estágio.

```
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
				/* colocar os jobs do estágio de Checkout aqui */
			}
		}
		stage('Build + Unit tests') {
			steps {
				/* colocar os jobs do estágio de Build e testes unitários aqui */
			}
		}
		stage('Archiving Reports') {
			steps {
				/* colocar os jobs do estágio de arquivamento de relatórios aqui */
			}
		}
		stage('BDD tests job'){
			steps {
				/* colocar os jobs do estágio de execução de BDD aqui */
			}
		}
	}
}
```

## Post

A seções Post define uma ou mais etapas adicionais que são executadas após a conclusão de uma execução de pipeline ou estágio (dependendo do local da seção do post no pipeline). O post pode suportar qualquer um dos seguintes blocos de pós-condição: `always`, `changed`, `fixed`, `regression`, `aborted`, `failure`, `success`, `unstable`, `unsuccessful`, and `cleanup`. Esses blocos de condições permitem a execução de etapas dentro de cada condição, dependendo do status de conclusão do pipeline ou estágio. 

Utilizaremos o bloco **always**, pois queremos que as etapas sejam executadas, independentemente do status de conclusão da execução do pipeline ou do estágio.

```
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
				/* colocar os jobs do estágio de Checkout aqui */
			}
		}
		stage('Build + Unit tests') {
			steps {
				/* colocar os jobs do estágio de Build e testes unitários aqui */
			}
		}
		stage('Archiving Reports') {
			steps {
				/* colocar os jobs do estágio de arquivamento de relatórios aqui */
			}
		}
		stage('BDD tests job'){
			steps {
				/* colocar os jobs do estágio de execução de BDD aqui */
			}
		}
	}
	post {
		always {
			/* colocar as ações do bloco post aqui */
		}
	}
}
```

# Versão Final

```
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
				slackSend channel: 'jenkins-ci', color: '#33AFFF', message: "*STARTED*: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'\n *More info at:* ${env.BUILD_URL}", teamDomain: 'team_domain', tokenCredentialId: 'slack'
				git branch: 'dev', credentialsId: 'aws', url: 'https://github.com/ms-test'
			}
		}
		stage('Build + Unit tests') {
			steps {
				sh 'mvn clean test'
			}
		}
		stage('Archiving Reports') {
			steps {
				dir(path: '.') {
					publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'target/site/jacoco/', reportFiles: 'index.html', reportName: 'Code Coverage', reportTitles: 'Code Coverage'])
					step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/TEST-*.xml'])
				}
			}
		}
		stage('BDD tests job'){
			steps {
				build job: 'bdd-ms-test', wait: true
			}
		}
	}
	post {
		always {
			slackSend channel: 'jenkins-ci', teamDomain: 'team_domain', tokenCredentialId: 'slack',
			color: COLOR_MAP[currentBuild.currentResult],
			message: "*${currentBuild.currentResult}:* Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'\n *More info at:* ${env.BUILD_URL}"
		}
	}
}
```
