# IMPORTANTE: Para evitar error de EOL ejecutar `git config --local core.autocrlf false` al descargar el proyecto, descartar cambios si los hubiera y hacer un checkout para tener los archivos .sh en el formato correcto.

# SELENIUM

Pruebas de aceptación basadas en Selenium.

## ARCHIVOS IMPORTANTES
* `\src\test\java\webdriver\WebDriverFactory.java` 
	Contiene la configuracion del navegador para las pruebas. Contiene las siguientes opciones:
	-	`options.addArguments("--no-sandbox");` arrancar navegador con Bypass OS
	-	`options.addArguments("--disable-dev-shm-usage");` solo para linux, la particion de /dev/shm es muy pequeña en ciertos entorno de VM causando que Chrome falle
	-	`options.addArguments("--verbose");` muestra información de la ejecución

* `src\test\resources\application-ic.properties | application-local.properties ` 
	Contiene las variables de entorno para la configuración del proyecto, una de ellas es:
	-	`URL_APLICACION` URL de la aplicación. Dependiendo del entorno contra el que se quiera ejecutar, se usará:
		- `host.docker.internal`:4200/agenciaagrariaypesquera/agro360/acceso, como dirección específica de "Docker for Windows" para poder tener acceso a la red local de la maquina host y poder lanzar las pruebas en localhost.
		- `https://ic.agapa.junta-andalucia.es`/agenciaagrariaypesquera/agro360/acceso, cuando se prueba contra integración continua.

* WebDriver. Se adjunta link de versiones del driver por si en local tiene conflictos (en docker se actualiza en la ejecución): https://chromedriver.storage.googleapis.com/index.html

## COMO EJECUTAR EL PROYECTO:
### DOCKER:
* REQUISITOS: Docker, Docker-Compose

* Una vez se tenga la imagen `agro360-pruebas:0.6.0.0`, se puede desplegar en dos entornos:
* IC:
* 	Usando docker run:
	- `./run-docker.bat`

* LOCAL:
* 	Usando docker run: 
	- `./run-docker-local.bat`
