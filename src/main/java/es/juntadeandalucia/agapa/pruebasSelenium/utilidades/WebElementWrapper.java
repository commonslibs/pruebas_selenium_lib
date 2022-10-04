package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest.PropiedadesTest;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * Clase que contiene las funcionalidades de selenium encapsulada, para usarlo de manera mas sencilla
 *
 * @author ISOTROL
 */
@Slf4j
public class WebElementWrapper {

   private WebDriver           driver;

   private final static String COLOR_AMARILLO         = "yellow";

   private final static String COLOR_AZUL             = "cyan";

   private final static int    NUMERO_MAXIMO_INTENTOS = 5;

   public WebElementWrapper(WebDriver driver) {
      this.driver = driver;
   }

   /**
    * Acción de seleccionar un elemento con etiqueta @param labelValue de un combo (select desplegable) identificado por el @param
    * testObject, incluye una comprobación para verificar de que el elemento a desaparecido.
    *
    * @param testObject,
    *           objeto combo
    * @param labelValue,
    *           etiqueta que se quiere seleccionar del combo
    * @throws PruebaAceptacionExcepcion
    */
   public void seleccionarElementoComboConEspera(By testObject, String labelValue) throws PruebaAceptacionExcepcion {
      this.click(testObject);
      this.click(By.xpath("//span[text() = '" + labelValue + "']"));
      // Enviamos el label con el texto, para saber si ese elemento ya se ha eliminado.
      this.esperarHastaQueElementoNoSeaVisible(By.xpath("//span[text() = '" + labelValue + "']"));
   }

   /**
    * Acción de seleccionar un elemento con etiqueta @param labelValue de un combo (select desplegable) identificado por el @param
    * testObject.
    *
    * @param testObject,
    *           objeto combo
    * @param labelValue,
    *           etiqueta que se quiere seleccionar del combo
    * @throws PruebaAceptacionExcepcion
    */
   public void seleccionarElementoCombo(By testObject, String labelValue) throws PruebaAceptacionExcepcion {
      this.click(testObject);
      this.click(By.xpath("//span[text() = '" + labelValue + "']"));
   }

   public WebElement click(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("click->" + testObject.toString());
      boolean conseguido = false;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {

            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            this.esperarHastaQueElementoClickable(elemento).click();
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al hacer click en el elemento. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }

      return elemento;
   }

   public void doubleClick(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("doubleClick->" + testObject.toString());
      boolean conseguido = false;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);

            this.esperarHastaQueElementoClickable(elemento);
            Actions actions = new Actions(this.driver);
            actions.doubleClick(elemento).perform();
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al hacer click en el elemento. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }

   }

   public void escribeTexto(By testObject, String texto) throws PruebaAceptacionExcepcion {
      log.debug("escribeTexto->" + testObject.toString() + ". Texto=" + texto);

      boolean conseguido = false;

      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.click(testObject);

            while (elemento.getAttribute("value").length() > 0) {
               // necesario porque así se soluciona donde aparece el cursor al hacer click (izq
               // o der)
               elemento.sendKeys(Keys.DELETE.toString());
               elemento.sendKeys(Keys.BACK_SPACE.toString());
            }
            for (int x = 0; x < texto.length(); x++) {
               elemento.sendKeys(texto.substring(x, x + 1));
            }
            elemento.sendKeys(Keys.TAB.toString()); // Hay veces que si no se pulsa TAB, no funciona
            conseguido = true;
         }
         catch (Exception e) {
            conseguido = false;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al escribir texto. Motivo del error";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void selectOptionByIndex(By testObject, Integer index) throws PruebaAceptacionExcepcion {
      log.debug("selectOptionByIndex->" + testObject.toString() + ". Index=" + index);
      boolean conseguido = false;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);

            Select comboBox = new Select(elemento);
            comboBox.selectByIndex(index);
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al hacer seleccionar el valor del combo por índice. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void selectOptionByLabel(By testObject, String label) throws PruebaAceptacionExcepcion {
      log.debug("selectOptionByLabel->" + testObject.toString() + ". Label=" + label);
      boolean conseguido = false;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);

            Select comboBox = new Select(elemento);
            comboBox.selectByVisibleText(label);
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje =
               "Error al hacer seleccionar el valor del combo por etiqueta. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyOptionSelectedByLabel(By testObject, String label) throws PruebaAceptacionExcepcion {
      log.debug("verifyOptionSelectedByLabel->" + testObject.toString() + ". Label=" + label);
      boolean conseguido = false;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      String etiquetaSeleccionada = "";
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            Select comboBox = new Select(elemento);
            etiquetaSeleccionada = comboBox.getFirstSelectedOption().getText().trim();
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje =
               "Error al hacer seleccionar el valor del combo por etiqueta. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      else { // Se ha obtenido el elemento web. Ahora se comprueba el valor del atributo
         if (!label.equals(etiquetaSeleccionada)) {
            String mensaje = "En el combo no está seleccionado con el valor " + label;
            log.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
   }

   public void selectOptionByValue(By testObject, String value) throws PruebaAceptacionExcepcion {
      log.debug("selectOptionByValue->" + testObject.toString() + ". Value=" + value);
      boolean conseguido = false;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);

            Select comboBox = new Select(elemento);
            comboBox.selectByValue(value);
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje =
               "Error al hacer seleccionar el valor del combo por value del option. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public String obtieneValorSeleccionadoEnCombo(By testObject, String selector) throws PruebaAceptacionExcepcion {
      log.debug("obtieneValorSeleccionadoEnCombo->" + testObject.toString() + ". Selector=" + selector);
      boolean conseguido = false;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      String valorSeleccionado = "";
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);

            Select comboBox = new Select(elemento);
            valorSeleccionado = comboBox.getFirstSelectedOption().getText();
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje =
               "Error al hacer seleccionar el valor del combo por value del option. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return valorSeleccionado;
   }

   public void verifyElementText(By testObject, String text) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementText->" + testObject.toString() + ". Text=" + text);
      boolean conseguido = false;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;

      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);

            log.debug("Comprobando que " + text + " es igual que " + elemento.getText());
            if (elemento != null && text.equals(elemento.getText())) {
               conseguido = true;
            }
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al verificar el texto del elemento.";
         if (excepcion != null) {
            mensaje += "Motivo del error: " + excepcion.getLocalizedMessage();
         }
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Realiza una espera activa mientras exista el @param testObject con el @textoAComprobar. Puede provocar un bloqueo, así que usarlo con
    * cuidado
    *
    * @param testObject
    * @param text
    * @throws PruebaAceptacionExcepcion
    */
   public void waitUntilElementTextChangedOrDisapeared(By testObject, String text) throws PruebaAceptacionExcepcion {
      log.debug("waitUntilElementTextChangedOrDisapeared->" + testObject.toString() + ". Text=" + text);
      boolean conseguido = false;
      WebElement elemento = null;

      elemento = this.esperaBasica(testObject);
      this.resaltaObjeto(elemento, COLOR_AMARILLO);

      while (!conseguido) {
         try {
            elemento = this.esperaBasica(testObject);

            if (elemento == null) {
               // Ha desaparecido --> ok
               conseguido = true;
            }
            else if (StringUtils.isBlank(elemento.getText())) {
               conseguido = true;
            }
            else if (!text.equals(elemento.getText())) {
               conseguido = true;
            }

            if (conseguido) {
               this.resaltaObjeto(elemento, COLOR_AZUL);
            }

         }
         catch (Exception e) {
            log.debug("waitUntilElementTextChangedOrDisapeared", e);
            conseguido = false;
            log.debug("se sigue intentando ...");
         }
      }
   }

   public boolean verifyElementPresentWithReturn(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementPresentWithReturn->" + testObject.toString());
      if (!this.isObjetoPresente(testObject)) {
         return false;
      }
      return true;
   }

   public void verifyElementPresent(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementPresent->" + testObject.toString());
      if (!this.isObjetoPresente(testObject)) {
         String mensaje = "El objeto con tipo selector no está presente cuando debería.";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementNotPresent(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementNotPresent->" + testObject.toString());
      if (!this.isObjetoNoPresente(testObject)) {
         String mensaje = "El objeto con tipo selector está presente cuando no debería.";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementChecked(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementChecked->" + testObject.toString());
      boolean conseguido = this.isElementChecked(testObject);
      if (!conseguido) {
         String mensaje = "El objeto con tipo selector no está checkeado cuando debería";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementNotChecked(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementNotChecked->" + testObject.toString());
      boolean conseguido = this.isElementChecked(testObject);
      if (conseguido) {
         String mensaje = "El objeto con tipo selector está checkeado cuando no debería";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public String getAttribute(By testObject, String atributo) throws PruebaAceptacionExcepcion {
      log.debug("getAttribute->" + testObject.toString() + ". Atributo=" + atributo);
      boolean conseguido = false;
      String valorAtributo = "";
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            valorAtributo = elemento.getAttribute(atributo);
            conseguido = true;
         }
         catch (Exception e) {
            conseguido = false;
            excepcion = new PruebaAceptacionExcepcion(e.getLocalizedMessage());
         }
      }
      if (!conseguido) {
         String mensaje = "Error al obtener el atributo del elemento web. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return valorAtributo;
   }

   public void verifyElementAttributeValue(By testObject, String atributo, String value) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementAttributeValue->" + testObject.toString() + ". Atributo=" + atributo + ". Value=" + value);
      boolean conseguido = false;
      String valorAtributo = "";
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            valorAtributo = elemento.getAttribute(atributo);
            conseguido = true;

         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al obtener el atributo del elemento web. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      else { // Se ha obtenido el elemento web. Ahora se comprueba el valor del atributo
         if (!value.equals(valorAtributo)) {
            String mensaje = "El valor " + value + " del atributo " + atributo + " no es el esperado (" + valorAtributo + ")";
            log.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
   }

   public void clickParaUploadFichero(By testObject, String rutaFichero) throws PruebaAceptacionExcepcion {
      log.debug("clickParaUploadFichero->" + testObject.toString() + ". RutaFichero=" + rutaFichero);
      boolean conseguido = false;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            this.esperarHastaQueElementoClickable(elemento);
            elemento.sendKeys(rutaFichero);
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al hacer click para subir fichero en el elemento. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public String getText(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("getText->" + testObject.toString());
      boolean conseguido = false;
      String cadena = "";
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            cadena = elemento.getText();
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al obtener el texto del elemento. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return cadena;
   }

   /**
    * Devuelve el numero de opciones presentes en el select cuyo objeto de test se pasa como parametro.
    *
    * @param testObject
    *           objeto de test consdierado.
    * @return el numero de opciones presentes en el select cuyo objeto de test se pasa como parametro.
    * @throws PruebaAceptacionExcepcion
    */
   public int cuentaNumeroDeOpcionesEnSelect(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("cuentaNumeroDeOpcionesEnSelect->" + testObject.toString());
      boolean conseguido = false;
      int numeroOpciones = 0;
      WebElement elemento = null;
      PruebaAceptacionExcepcion excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);

            Select comboBox = new Select(elemento);
            List<WebElement> listOptionDropdown = comboBox.getOptions();
            numeroOpciones = listOptionDropdown.size();
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al hacer seleccionar el valor del combo por índice. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return numeroOpciones;
   }

   /**
    * Obtiene la fila de una tabla que contiene el @param texto en alguna de sus columnas. Las filas están numeradas de 0 en adelante. Si no
    * encuentra el texto en ninguna fila, devuelve -1.
    *
    * @param driver
    *           valor para: driver
    * @param testObject
    *           valor para: Locator del elemento tabla
    * @param texto
    *           valor para: texto
    * @return int
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public int obtenerFilaConTextoEnTabla(By testObject, String texto) throws PruebaAceptacionExcepcion {
      log.debug("obtenerFilaConTextoEnTabla->" + testObject.toString() + ". Texto=" + texto);
      int fila = -1;
      boolean conseguido = false;
      WebElement table = null;
      List<WebElement> rows = null;
      PruebaAceptacionExcepcion excepcion = null;
      String textoEsperado = texto;

      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            // Localizar el body de la tabla
            table = this.esperaBasica(testObject);
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }

      if (conseguido && table != null) {
         // Se obtienen todas las filas de la tabla
         rows = table.findElements(By.tagName("tr"));
         // Se recorren todas las filas de la tabla
         for (int i = 0; i < rows.size(); i++) {
            // Se recorre columna por columna buscando el texto esperado
            List<WebElement> cols = rows.get(i).findElements(By.tagName("td"));
            for (WebElement col : cols) {
               if (col.getText().trim().equalsIgnoreCase(textoEsperado)) {
                  fila = i;
                  break;
               }
            }

         }
      }
      else {
         String mensaje = "Error al obtener el id del cuerpo de la tabla. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }

      if (fila == -1) {
         String mensaje = "No se encuentra la fila de la tabla con idBodyTabla: " + table.getAttribute("id") + " y texto buscado: " + texto;
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }

      return fila;
   }

   public int obtieneNumeroDeFilasListado(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("obtieneNumeroDeFilasListado->" + testObject.toString());
      boolean conseguido = false;
      WebElement table = null;
      List<WebElement> rows = null;
      PruebaAceptacionExcepcion excepcion = null;
      int numeroDeFilas = 0;

      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {

            // Localizar el body de la tabla
            // FIXME : No hay forma de esperar después del evento filtrado, debido al evento
            // onKeyUp que mete un retraso aleatorio en la carga
            // del listado.
            Thread.sleep(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_CORTO.name())) * 1000);
            table = this.esperaBasica(testObject);
            rows = table.findElements(By.tagName("tr"));
            numeroDeFilas = rows.size();
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
         catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
         }
      }

      if (!conseguido) {
         String mensaje = "Error al hacer seleccionar el valor del combo por índice. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }

      return numeroDeFilas;
   }

   public void esperarFilaUnicaListado(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("esperarFilaUnicaListado->" + testObject.toString());
      this.esperarListadoConFilas(testObject, 1);
   }

   public void esperarListadoConFilas(By testObject, int numeroFilasEsperadas) throws PruebaAceptacionExcepcion {
      log.debug("esperarListadoConFilas->" + testObject.toString() + ". NumeroFilasEsperadas=" + numeroFilasEsperadas);
      boolean conseguido = false;
      WebElement table = null;
      List<WebElement> rows = null;
      PruebaAceptacionExcepcion excepcion = null;
      int numeroDeFilas = 0;

      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            // Localizar el body de la tabla
            // FIXME : No hay forma de esperar después del evento filtrado, debido al evento
            // onKeyUp que mete un retraso aleatorio en la carga
            // del listado.
            this.esperaIncondicional(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_CORTO.name())));
            table = this.esperaBasica(testObject);
            rows = table.findElements(By.tagName("tr"));
            numeroDeFilas = rows.size();
            if (numeroDeFilas == numeroFilasEsperadas) {
               conseguido = true;
            }
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
            excepcion = e;
         }
      }

      if (!conseguido) {
         String mensaje = "Error al hacer seleccionar el valor del combo por índice. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void esperaIncondicional(int segundos) {
      log.debug("esperaIncondicional-> " + segundos + " segundos");
      try {
         Thread.sleep(segundos * 1000);
      }
      catch (InterruptedException e) {
         // Seguramente nunca se produzca esta excepción
         log.error("Error en espera activa");
      }
   }

   /**
    * Posicionar en porsición más alta de la página
    */
   public void scrollTopPagina() {
      log.debug("scrollTopPagina");
      ((JavascriptExecutor) this.driver).executeScript("window.scrollTo(0, -document.body.scrollHeight)");
   }

   /**
    * Devuelve el nº de elementos de la @param tabla
    *
    * @param tabla,
    *           identificador de la tabla de la que se quiere tener el nº de registros
    * @return nº de registros de la @param tabla
    * @throws PruebaAceptacionExcepcion
    */
   public int obtenerNumeroRegistrosTabla(By tabla) throws PruebaAceptacionExcepcion {
      log.debug("obtenerNumeroRegistrosTabla->" + tabla.toString());
      WebElement t = this.esperaBasica(tabla);

      // Identificador de la informacion del nº de registros del listado. (Ej: 1-10 de
      // 100 resultados)
      By checkSeleccionarLoteAnalisis = By.cssSelector("span.ui-paginator-current");
      WebElement paginador = t.findElement(checkSeleccionarLoteAnalisis);

      String cadena = paginador.getText();
      String[] aux = cadena.split(" ");
      String res = aux[aux.length - 2];
      Integer.valueOf(res);

      return Integer.valueOf(res);
   }

   public void esperarHastaQueElementoNoPresente(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("esperarHastaQueElementoNoPresente->" + testObject.toString());
      WebDriverWait wait = new WebDriverWait(this.driver,
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_LARGO.name()))),
            Duration.ofMillis(100));
      try {
         wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(testObject)));
      }
      catch (TimeoutException e) {
         String mensaje = "Error al esperar que el objeto " + testObject + " NO esté presente";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   private boolean isElementChecked(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("isElementChecked->" + testObject.toString());
      boolean conseguido = false;
      WebElement elemento = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = elemento.isSelected();
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
         }
      }
      if (!conseguido) {
         String mensaje = "El objeto " + testObject + " NO está chequeado";
         log.error(mensaje);
      }
      return conseguido;
   }

   private boolean isObjetoPresente(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("isObjetoPresente->" + testObject.toString());
      boolean conseguido = false;
      WebElement elemento = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaBasica(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
         }
      }
      if (!conseguido) {
         String mensaje = "El objeto " + testObject + " NO está presente";
         log.error(mensaje);
      }
      return conseguido;
   }

   private boolean isObjetoNoPresente(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("isObjetoNoPresente->" + testObject.toString());
      boolean conseguido = false;
      try {
         this.esperaBasica(testObject);
      }
      catch (PruebaAceptacionExcepcion e) {
         conseguido = true;
      }
      if (!conseguido) {
         String mensaje = "El objeto " + testObject + " está presente";
         log.error(mensaje);
      }
      return conseguido;
   }

   private void esperarDesaparezcaProcesando(WebDriver driver) throws PruebaAceptacionExcepcion {
      log.debug("esperarDesaparezcaProcesando");

      String idElementoProcesando = null;
      try {
         idElementoProcesando = VariablesGlobalesTest.getPropiedad(PropiedadesTest.ID_ELEMENTO_PROCESANDO.name());
      }
      catch (IllegalArgumentException e) {
         log.trace("ID_ELEMENTO_PROCESANDO no definido en fichero properties");
      }
      if (idElementoProcesando != null) {
         By by = By.id(idElementoProcesando);
         driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1));
         List<WebElement> elementos = driver.findElements(by);
         try {
            if (elementos.size() > 0) {
               if (elementos.get(0).isDisplayed()) {
                  int tiempo = 0;
                  while (elementos.get(0).isDisplayed()) {
                     try {
                        Thread.sleep(100);
                     }
                     catch (InterruptedException e) {
                        // Seguramente nunca se produzca esta excepción
                        log.error("Error al parar el procesamiento del hilo de ejecución");
                     }
                     tiempo += 100;
                     if (tiempo > Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO.name()))
                           * 1000) {
                        String mensaje = "La ventana \"Procesando...\" no desaparece";
                        log.error(mensaje);
                        throw new PruebaAceptacionExcepcion(mensaje);
                     }
                  }
               }
            }
         }
         catch (StaleElementReferenceException e) {
            log.debug("La ventana procesando ya no está visible");
         }
      }
   }

   public WebElement esperaBasica(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("esperaBasica->" + testObject.toString());
      this.esperarDesaparezcaProcesando(this.driver);
      this.esperarHastaQueElementoPresente(testObject);
      return this.esperarHastaQueElementoVisible(testObject);
   }

   private WebElement esperarHastaQueElementoVisible(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("esperarHastaQueElementoVisible->" + testObject.toString());
      WebElement exito;
      WebDriverWait wait = new WebDriverWait(this.driver,
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO.name()))),
            Duration.ofMillis(100));
      try {
         exito = wait.until(ExpectedConditions.visibilityOfElementLocated(testObject));
      }
      catch (TimeoutException e) {
         String mensaje = "Error al esperar que el objeto " + testObject.toString() + " sea visible";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return exito;
   }

   /**
    * Método que permite saber si el @param ha dejado de ser visible. En caso de que siga visible lanza un error.
    *
    * @param testObject
    * @throws PruebaAceptacionExcepcion
    */
   private void esperarHastaQueElementoNoSeaVisible(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("esperarHastaQueElementoNoSeaVisible->" + testObject.toString());
      WebDriverWait wait = new WebDriverWait(this.driver,
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO.name()))),
            Duration.ofMillis(100));
      try {
         wait.until(ExpectedConditions.invisibilityOf(this.driver.findElement(testObject)));

      }
      catch (TimeoutException e) {
         String mensaje = "Error al esperar que el objeto " + testObject.toString() + " dejara de ser visible";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   private WebElement esperarHastaQueElementoPresente(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("esperarHastaQueElementoPresente->" + testObject.toString());
      WebElement exito;
      WebDriverWait wait = new WebDriverWait(this.driver,
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO.name()))),
            Duration.ofMillis(100));
      try {
         exito = wait.until(ExpectedConditions.presenceOfElementLocated(testObject));
      }
      catch (TimeoutException e) {
         String mensaje = "Error al esperar que el objeto " + testObject + " esté presente";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return exito;
   }

   private WebElement esperarHastaQueElementoClickable(WebElement testObject) throws PruebaAceptacionExcepcion {
      log.debug("esperarHastaQueElementoClickable->" + testObject.getAttribute("id"));
      WebElement exito;
      WebDriverWait wait = new WebDriverWait(this.driver,
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO.name()))),
            Duration.ofMillis(100));
      try {
         exito = wait.until(ExpectedConditions.elementToBeClickable(testObject));
      }
      catch (TimeoutException e) {
         String mensaje = "Error al esperar que el objeto " + testObject + " sea clickable";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return exito;
   }

   /**
    * Resalta el @element de @param color, útil para seguir una traza visual.
    *
    * @param driver
    * @param element
    * @param color
    * @throws PruebaAceptacionExcepcion
    */
   private void resaltaObjeto(WebElement element, String color) throws PruebaAceptacionExcepcion {
      log.trace("resaltaObjeto->" + element.toString() + ". Color=" + color);
      try {
         JavascriptExecutor js = (JavascriptExecutor) this.driver;
         js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element,
               "background: " + color + "; color: black; border: 3px solid black;");
      }
      catch (Exception e) {
         String mensaje = "El elemento " + element + " no puede ser resaltado con color";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Comprueba si el elemento extiste y luego obtiene el texto. El obejeto no tiene porque estar presente.
    *
    * @param testObject
    * @return si no existe el elemento devuelve "", sino el valor.
    * @throws PruebaAceptacionExcepcion
    */
   public String obtenerTextoElemento(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("obtenerTextoElemento->" + testObject.toString());
      WebElement elemento = null;
      try {
         if (this.isObjetoPresente(testObject)) {
            elemento = this.esperaBasica(testObject);

            this.resaltaObjeto(elemento, COLOR_AZUL);
            return elemento.getText();
         }

      }
      catch (PruebaAceptacionExcepcion e) {
         String mensaje = "Error al verificar el texto del elemento. Motivo del error: " + e.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }

      return "";
   }

}
