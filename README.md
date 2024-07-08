# Librería común para pruebas basadas en SELENIUM

Esta librería define una serie de métodos comunes para utilizar en las pruebas funcionales basadas en Selenium.

## CONFIGURACIÓN DE LAS PRUEBAS

En el fichero de properties de la aplicación que se vaya a usar deberán existir las siguientes propiedades:
- URL_APLICACION. URL en la que se encuentra la aplicación a probar.
- NAVEGADOR. Obligatorio. Posibles valores:
	- CHROME
	- FIREFOX
	- MSEDGE
- MAXIMIZAR. Opcional. Posibles valores:
	- TRUE. Es el valor por defecto. El navegador se maximizará hasta ocupar la pantalla completa al inicio de cada prueba.
	- FALSE
- TIEMPO_RETRASO_CORTO. Obligatorio. Tiempo en segundos de retraso mas corto posible. Valor típico: 1.
- TIEMPO_RETRASO_MEDIO Obligatorio. Tiempo en segundos de retraso medio. Valor típico: 5.
- TIEMPO_RETRASO_LARGO Obligatorio. Tiempo en segundos de retraso mas largo posible. Valor típico: 10.
- ID_ELEMENTO_PROCESANDO. Opcional. Identificador del elemento que se intentará localizar antes de cada acción. Si está visible, se esperará a que desaparezca antes de realizar la siguiente acción.
- POSICION_CERTIFICADO. Posición del certificado digital a usar dentro de la lista de certificados que ofrece el navegador.

En el fichero properties que se vaya a usar podrán existir otras propiedades adicionales que se estimen necesarias.

El proyecto usa la librería http://automation-remarks.com/video-recorder-java/ para guardar vídeos de las pruebas. Opcionalmente, es posible añadir propiedades adicionales al fichero properties usado, con las propiedades que permite el propio componente para refinar la configuración de los vídeos generados.

Prueba1
Segundo2