
Configuração do Servlet:

Assumindo a instalacao do Java JDK1.7 como segue:

- add-apt-repository ppa:webupd8team/java && sudo apt-get update
- apt-get install oracle-jdk7-installer

Instalar a ferramenta de deploy Ant:
- apt-get install ant

Eh necessario instalar o servidor JSP apache-tomcat.
No caso, utilizei a versão 7.05.

- wget http://ftp.unicamp.br/pub/apache/tomcat/tomcat-7/v7.0.50/bin/apache-tomcat-7.0.50.tar.gz ~/Downloads
- tar xvfz ~/Downloads/apache-tomcat-7.0.50.tar.gz /usr/local
- ln -s apache-tomcat-7.0.50 apache-tomcat

No arquivo ~/.bashrc:

...
- Tomcat
export CATALINA_HOME=/usr/local/apache-tomcat
export PATH=$PATH:$CATALINA_HOME/bin
- JRE
export JRE_HOME=/usr/lib/jvm/java-7-oracle/jre
...

- source ~/.bashrc

Editar o arquivo de regras para ter acesso ah interface web do tomcat.
Editar o arquivo $CATALINA_HOME/conf/tomcat-users.xml:
...
<role rolename="manager-gui"/>
<role rolename="manager-script"/>
<role rolename="admin-gui"/>
<role rolename="admin-script"/>
<user username="admin" password="root" roles="manager-gui,manager-script,admin-gui,admin-script"/>
...

Copiar os arquivos de dependencia do Ant:

- cp apache-tomcat/bin/tomcat-juli.jar /usr/share/ant/lib   <--- Aqui eh 'bin'!

- cp apache-tomcat/lib/tomcat-util.jar /usr/share/ant/lib   <--- Aqui eh 'lib'!
- cp apache-tomcat/lib/tomcat-coyote.jar /usr/share/ant/lib
- cp apache-tomcat/lib/catalina-ant.jar /usr/share/ant/lib

Primeiro deploy:
- ant all

Para os demais:
- ant remove all

Verifique o funcionamento no browser: http://localhost:8080/SimpleCloud

- Fixed bug in update servlet (clean property must delete 'dist' directory)


---Git

Create a new repository on the command line (Note: a previous repository
folder named 'simplecloud' was created in github.com site first!)

- git init
- git add *
- git commit -am "meu commit"
- git remote add origin https://github.com/simplecloudservlet/simplecloud.git
- git push -u origin master

After first commit, to avoid inform username and password each time:

- git config --global credential.helper cache
- git config --global credential.helper 'cache --timeout=3600'

To update changes on repository:
- git commit -am "meu commit"
- git push -u origin master

===
To acquire status of modifications:
- git status

====
To remove git unwanted configuration in the project:
- rm -rf .git

To remove git unwanted cached config (as username and password):
- rm -rf ~/.git*


