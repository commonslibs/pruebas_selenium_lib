package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest.PropiedadesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.xpath.ConditionType;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.xpath.TestObject;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.xpath.XPathBuilder;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
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

   private final static String COLOR_AMARILLO         = "yellow"; // Interacción

   private final static String COLOR_AZUL             = "cyan";   // Comprobación

   private final static int    NUMERO_MAXIMO_INTENTOS = 5;

   private final static int    NUMERO_MINIMO_INTENTOS = 2;

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
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elementoEsperado = this.esperaCompleta(testObject);
            this.resaltaObjeto(elementoEsperado, COLOR_AMARILLO);
            elemento = this.esperarHastaQueElementoClickable(testObject);
            elemento.click();
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al hacer click en el elemento. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return elemento;
   }

   public void doubleClick(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("doubleClick->" + testObject.toString());
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);

            this.esperarHastaQueElementoClickable(elemento);
            Actions actions = new Actions(WebDriverFactory.getDriver());
            actions.doubleClick(elemento).perform();
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al hacer click en el elemento. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void escribeTexto(By testObject, String texto) throws PruebaAceptacionExcepcion {
      log.debug("escribeTexto->" + testObject.toString() + ". Texto=" + texto);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.click(testObject);
            if (elemento.getAttribute("value").length() > 0) {
               // Para borrar campos que tienen mucho texto y así sea mas rápido
               elemento.clear();
               elemento = this.esperaCompleta(testObject);
            }
            if (elemento.getAttribute("value").length() > 0) {
               // Para borrar campos numéricos formateados con deciamles
               while (elemento.getAttribute("value").length() > 0) {
                  // Necesario porque así se soluciona donde aparece el cursor al hacer click (izq o der)
                  elemento.sendKeys(Keys.DELETE.toString());
                  elemento.sendKeys(Keys.BACK_SPACE.toString());
               }
               elemento = this.esperaCompleta(testObject);
            }
            for (int x = 0; x < texto.length(); x++) {
               elemento.sendKeys(texto.substring(x, x + 1));
            }
            elemento.sendKeys(Keys.TAB.toString()); // Hay veces que si no se pulsa TAB, no funciona
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
            log.error(e.getLocalizedMessage());
         }
      }
      if (!conseguido) {
         String mensaje = "Error al escribir texto en " + testObject + ". Motivo del error : " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void subirFichero(By testObject, String ruta) throws PruebaAceptacionExcepcion {
      log.debug("subirFichero->" + testObject.toString() + ". Texto=" + ruta);
      this.clickParaUploadFichero(testObject, ruta);
      // boolean conseguido = false;
      //
      // for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
      // try {
      // this.esperarDesaparezcaProcesando();
      // WebElement elemento = this.esperarHastaQueElementoPresente(testObject);
      // elemento.sendKeys(ruta);
      // conseguido = true;
      // }
      // catch (Exception e) {
      // conseguido = false;
      // }
      // }
      // if (!conseguido) {
      // String mensaje = "Error al escribir texto sin borrar. Motivo del error";
      // log.error(mensaje);
      // throw new PruebaAceptacionExcepcion(mensaje);
      // }
   }

   public void selectOptionByIndex(By testObject, Integer index) throws PruebaAceptacionExcepcion {
      log.debug("selectOptionByIndex->" + testObject.toString() + ". Index=" + index);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            this.esperaCompleta(testObject);
            WebElement elemento = this.esperarHastaQueElementoClickable(testObject);

            Select comboBox = new Select(elemento);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            comboBox.selectByIndex(index);
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al hacer seleccionar el valor del combo por índice. Motivo del error: "
               + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Selecciona un valor en un combo cuyo texto se pasa por parámetro. Si el combo está deshabilitado no se hará ningún cambio, solo se
    * comprobará que el texto coincida con el parámetro pasado.
    *
    * @param testObject
    * @param label
    * @throws PruebaAceptacionExcepcion
    */
   public void selectOptionByLabel(By testObject, String label) throws PruebaAceptacionExcepcion {
      log.debug("selectOptionByLabel->" + testObject.toString() + ". Label=" + label);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            // WebElement elemento = this.esperarHastaQueElementoClickable(testObject);

            Select comboBox = new Select(elemento);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            if (elemento.isEnabled()) {
               comboBox.selectByVisibleText(label);
               conseguido = true;
            }
            else if (comboBox.getFirstSelectedOption().getText().equals(label)) {
               conseguido = true;
            }
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al hacer seleccionar el valor del combo por etiqueta. Motivo del error: "
               + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void selectOptionByLabel(By testObject, String label, boolean conRetraso) throws PruebaAceptacionExcepcion {
      log.debug("selectOptionByLabel->" + testObject.toString() + ". Label=" + label + ". Con retraso=" + conRetraso);
      try {
         this.selectOptionByLabel(testObject, label);
         if (conRetraso) {
            this.esperaCorta();
         }
      }
      catch (PruebaAceptacionExcepcion e) {
         String mensaje = "Error en " + testObject + " al hacer seleccionar el valor del combo por etiqueta con retraso. Motivo del error: "
               + e.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyOptionSelectedByLabel(By testObject, String label) throws PruebaAceptacionExcepcion {
      log.debug("verifyOptionSelectedByLabel->" + testObject + ". Label=" + label);
      boolean conseguido = false;
      Exception excepcion = null;
      String etiquetaSeleccionada = "";
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            Select comboBox = new Select(elemento);
            etiquetaSeleccionada = comboBox.getFirstSelectedOption().getText().trim();
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error  en " + testObject + "al hacer seleccionar el valor del combo por etiqueta. Motivo del error: "
               + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      else if (!label.equals(etiquetaSeleccionada)) {
         String mensaje = "En el combo no está seleccionado con el valor " + label;
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Verifica que la opción con la etiqueta label SI está presente en el combo testObject
    *
    * @param testObject
    * @param label
    * @return
    * @throws PruebaAceptacionExcepcion
    */
   public void verifyOptionPresentByLabel(By testObject, String label) throws PruebaAceptacionExcepcion {
      log.debug("verifyOptionPresentByLabel->" + testObject + ". Label=" + label);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            Select comboBox = new Select(elemento);
            for (WebElement opcion : comboBox.getOptions()) {
               if (opcion.getText().equals(label)) {
                  conseguido = true;
                  break;
               }
            }
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "La opción " + label + " de " + testObject.toString() + " no está presente en " + testObject
               + ". Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Verifica que la opción con la etiqueta label no está presente en el combo testObject
    *
    * @param testObject
    * @param label
    * @return
    * @throws PruebaAceptacionExcepcion
    */
   public void verifyOptionNotPresentByLabel(By testObject, String label) throws PruebaAceptacionExcepcion {
      log.debug("verifyOptionNotPresentByLabel->" + testObject + ". Label=" + label);
      boolean encontrado = false;
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            encontrado = true;
            Select comboBox = new Select(elemento);
            for (WebElement opcion : comboBox.getOptions()) {
               if (opcion.getText().equals(label)) {
                  conseguido = true;
                  break;
               }
            }
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!encontrado || !conseguido) {
         String mensaje = "La opción " + label + " de " + testObject.toString() + " está presente cuando no debería en " + testObject
               + ". Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void selectOptionByValue(By testObject, String value) throws PruebaAceptacionExcepcion {
      log.debug("selectOptionByValue->" + testObject.toString() + ". Value=" + value);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            this.esperaCompleta(testObject);
            WebElement elemento = this.esperarHastaQueElementoClickable(testObject);

            Select comboBox = new Select(elemento);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            comboBox.selectByValue(value);
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al hacer seleccionar el valor del combo por value del option. Motivo del error: "
               + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public String obtieneValorSeleccionadoEnCombo(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("obtieneValorSeleccionadoEnCombo->" + testObject.toString());
      boolean conseguido = false;
      Exception excepcion = null;
      String valorSeleccionado = "";
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);

            Select comboBox = new Select(elemento);
            valorSeleccionado = comboBox.getFirstSelectedOption().getText();
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al hacer seleccionar el valor del combo por value del option. Motivo del error: "
               + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return valorSeleccionado;
   }

   public void verifyElementText(By testObject, String text) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementText->" + testObject.toString() + ". Text=" + text);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            if (elemento == null) {
               throw new PruebaAceptacionExcepcion(testObject.toString() + " no existe");
            }
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = this.esperarHastaQueElementoTengaTexto(testObject, text);
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje =
               "Error en " + testObject + " al verificar el texto del elemento " + testObject.toString() + ". Texto esperado: " + text;
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + excepcion.getLocalizedMessage();
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
      WebElement elemento = this.esperaCompleta(testObject);
      this.resaltaObjeto(elemento, COLOR_AZUL);

      while (!conseguido) {
         try {
            elemento = this.esperaCompleta(testObject);

            if ((elemento == null) || StringUtils.isBlank(elemento.getText()) || !text.equals(elemento.getText())) {
               // Ha desaparecido --> ok
               conseguido = true;
            }

            if (conseguido) {
               this.resaltaObjeto(elemento, COLOR_AMARILLO);
            }
         }
         catch (Exception e) {
            log.debug("waitUntilElementTextChangedOrDisapeared", e);
            conseguido = false;
            log.debug("se sigue intentando ...");
         }
      }
   }

   public void verifyElementPresent(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementPresent->" + testObject.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         if (this.isObjetoPresente(testObject)) {
            conseguido = true;
         }
      }
      if (!conseguido) {
         String mensaje = "El objeto " + testObject.toString() + " no está presente";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementNotPresent(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementNotPresent->" + testObject.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         if (this.esperarHastaQueElementoNoPresente(testObject)) {
            // this.esperaCorta();
            // if (this.isObjetoNoPresente(testObject)) {
            conseguido = true;
         }
      }
      if (!conseguido) {
         String mensaje = "El objeto " + testObject.toString() + " está presente";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementChecked(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementChecked->" + testObject.toString());
      boolean conseguido = this.isElementChecked(testObject);
      if (!conseguido) {
         String mensaje = "El objeto " + testObject + " no está checkeado cuando debería";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementNotChecked(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementNotChecked->" + testObject.toString());
      boolean marcado = this.isElementChecked(testObject);
      if (marcado) {
         String mensaje = "El objeto " + testObject + " está checkeado cuando no debería";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public String getAttribute(By testObject, String atributo) throws PruebaAceptacionExcepcion {
      log.debug("getAttribute->" + testObject.toString() + ". Atributo=" + atributo);
      boolean conseguido = false;
      String valorAtributo = "";
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            valorAtributo = elemento.getAttribute(atributo);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al obtener el atributo del elemento " + testObject + ". Motivo del error: "
               + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return valorAtributo;
   }

   public void verifyElementAttributeValue(By testObject, String atributo, String value) throws PruebaAceptacionExcepcion {
      log.debug("verifyElementAttributeValue->" + testObject.toString() + ". Atributo=" + atributo + ". Value=" + value);
      boolean conseguido = false;
      String valorAtributo = "";
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            valorAtributo = elemento.getAttribute(atributo);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje =
               "Error en " + testObject + " al obtener el atributo del elemento web. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      else if (!value.equals(valorAtributo)) {
         String mensaje = "El valor " + value + " del atributo " + atributo + " no es el esperado (" + valorAtributo + ")";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyTextPresent(String texto) throws PruebaAceptacionExcepcion {
      log.debug("verifyTextPresent->" + texto);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.encuentraTextoEnPagina(texto);
      }
      if (!conseguido) {
         String mensaje = "Texto " + texto + " no presente";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   private boolean encuentraTextoEnPagina(String texto) {
      boolean conseguido = false;
      try {
         this.esperaCorta();
         WebElement textDemo = WebDriverFactory.getDriver().findElement(By.xpath("//*[contains(text(),'" + texto + "')]"));
         if (textDemo.isDisplayed()) {
            this.resaltaObjeto(textDemo, COLOR_AMARILLO);
            conseguido = true;
         }
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
         conseguido = false;
      }
      return conseguido;
   }

   public void clickParaUploadFichero(By testObject, String rutaFichero) throws PruebaAceptacionExcepcion {
      log.debug("clickParaUploadFichero->" + testObject.toString() + ". RutaFichero=" + rutaFichero);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaParaSubirFichero(testObject);
            elemento.sendKeys(rutaFichero);
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al hacer click para subir fichero en el elemento. Motivo del error: "
               + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   private WebElement esperaParaSubirFichero(By testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperaParaSubirFichero->" + testObject.toString());
      this.esperarDesaparezcaProcesando();
      WebElement elemento = this.esperarHastaQueElementoPresente(testObject);
      this.resaltaObjeto(elemento, COLOR_AMARILLO);
      return elemento;
   }

   public String getText(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("getText->" + testObject.toString());
      boolean conseguido = false;
      String cadena = "";
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            if (i == 1) {
               this.esperaBreve(testObject);
            }
            WebElement elemento = this.esperaCompleta(testObject);
            cadena = elemento.getText();
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje =
               "Error en " + testObject + " al obtener el texto del elemento. Motivo del error: " + excepcion.getLocalizedMessage();
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
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);

            Select comboBox = new Select(elemento);
            List<WebElement> listOptionDropdown = comboBox.getOptions();
            numeroOpciones = listOptionDropdown.size();
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al hacer seleccionar el valor del combo por índice. Motivo del error: "
               + excepcion.getLocalizedMessage();
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
      Exception excepcion = null;
      String textoEsperado = texto;

      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            // Localizar el body de la tabla
            table = this.esperaCompleta(testObject);
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
         }
      }

      if (conseguido && table != null) {
         // Se obtienen todas las filas de la tabla
         List<WebElement> rows = table.findElements(By.xpath("//tr[contains(@class, 'rich-table-row')]"));
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

   public int obtieneNumeroDeFilasListado(String idBodyTabla) throws PruebaAceptacionExcepcion {
      log.debug("obtieneNumeroDeFilasListado->" + idBodyTabla);
      boolean conseguido = false;
      Exception excepcion = null;
      int numeroDeFilas = 0;

      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {

            // Localizar el body de la tabla
            // FIXME : No hay forma de esperar después del evento filtrado, debido al evento
            // onKeyUp que mete un retraso aleatorio en la carga del listado.
            // Thread.sleep(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_CORTO)) * 1000);
            WebElement table = this.esperaCompleta(By.id(idBodyTabla));
            List<WebElement> rows = table.findElements(By.xpath("//tr[contains(@class, 'rich-table-row')]"));
            numeroDeFilas = rows.size();
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
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
      Exception excepcion = null;
      int numeroDeFilas = 0;

      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            // Localizar el body de la tabla
            // FIXME : No hay forma de esperar después del evento filtrado, debido al evento onKeyUp que mete un retraso aleatorio en la
            // carga del listado.
            this.esperaIncondicional(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_CORTO)));
            table = this.esperaCompleta(testObject);
            List<WebElement> rows = table.findElements(By.xpath("//tr[contains(@class, 'rich-table-row')]"));
            numeroDeFilas = rows.size();
            if (numeroDeFilas == numeroFilasEsperadas) {
               conseguido = true;
            }
         }
         catch (Exception e) {
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject + " al hacer seleccionar el valor del combo por índice. Motivo del error: "
               + excepcion.getLocalizedMessage();
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
      ((JavascriptExecutor) WebDriverFactory.getDriver()).executeScript("window.scrollTo(0, -document.body.scrollHeight)");
   }

   public boolean esperarHastaQueElementoNoPresente(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("esperarHastaQueElementoNoPresente->" + testObject.toString());
      boolean conseguido = false;
      WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_LARGO))),
            Duration.ofMillis(100));
      try {
         conseguido = wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(testObject)));
      }
      catch (TimeoutException | StaleElementReferenceException e) {
         String mensaje = "Error al esperar que el objeto " + testObject + " NO esté presente";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return conseguido;
   }

   public boolean isElementChecked(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("isElementChecked->" + testObject.toString());
      boolean conseguido = false;
      boolean marcado = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            marcado = elemento.isSelected();
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = true;
         }
         catch (Exception e) {
            excepcion = e;
            log.error(e.getLocalizedMessage());
         }
      }
      if (!conseguido) {
         StringBuilder mensaje = new StringBuilder("El objeto ").append(testObject).append(" NO está chequeado.");
         if (excepcion != null) {
            mensaje.append(". Motivo del error: ").append(excepcion.getLocalizedMessage());
         }
         log.error(mensaje.toString());
      }
      return marcado;
   }

   public boolean isObjetoPresente(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("isObjetoPresente->" + testObject.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MINIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaBreve(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = true;
         }
         catch (Exception e) {
            log.error(e.getLocalizedMessage());
            conseguido = false;
         }
      }
      if (!conseguido) {
         String mensaje = "El objeto " + testObject + " NO está presente";
         log.error(mensaje);
      }
      return conseguido;
   }

   private void esperarDesaparezcaProcesando() throws PruebaAceptacionExcepcion {
      log.trace("esperarDesaparezcaProcesando");

      String idElementoProcesando = null;
      try {
         idElementoProcesando = VariablesGlobalesTest.getPropiedad(PropiedadesTest.ID_ELEMENTO_PROCESANDO);
      }
      catch (PruebaAceptacionExcepcion e) {
         log.trace("ID_ELEMENTO_PROCESANDO no definido en fichero properties");
      }
      if (idElementoProcesando != null) {
         By by = By.id(idElementoProcesando);
         WebDriverFactory.getDriver().manage().timeouts().implicitlyWait(Duration.ofMillis(1));
         List<WebElement> elementos = WebDriverFactory.getDriver().findElements(by);
         try {
            if ((elementos.size() > 0) && elementos.get(0).isDisplayed()) {
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
                  if (tiempo > Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO)) * 1000) {
                     String mensaje = "La ventana \"Procesando...\" no desaparece";
                     log.error(mensaje);
                     throw new PruebaAceptacionExcepcion(mensaje);
                  }
               }
            }
         }
         catch (StaleElementReferenceException e) {
            log.trace("La ventana procesando ya no está visible");
         }
      }
   }

   private WebElement esperaBreve(By testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperaBreve->" + testObject.toString());
      this.esperarDesaparezcaProcesando();
      this.esperarHastaQueElementoPresenteBreve(testObject);
      return this.esperarHastaQueElementoVisibleBreve(testObject);
   }

   public WebElement esperaCompleta(By testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperaCompleta->" + testObject.toString());
      this.esperarDesaparezcaProcesando();
      this.esperarHastaQueElementoPresente(testObject);
      return this.esperarHastaQueElementoVisible(testObject);
   }

   private WebElement esperarHastaQueElementoVisibleBreve(By testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperarHastaQueElementoVisibleBreve->" + testObject.toString());
      WebElement elemento = null;
      WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
            Duration.ofMillis(100));
      try {
         elemento = wait.until(ExpectedConditions.visibilityOfElementLocated(testObject));
      }
      catch (TimeoutException | StaleElementReferenceException e) {
         String mensaje = "Error al esperar brevemente que el objeto " + testObject.toString() + " sea visible";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return elemento;
   }

   public WebElement esperarHastaQueElementoVisible(By testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperarHastaQueElementoVisible->" + testObject.toString());
      WebElement exito = null;
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
               Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
               Duration.ofMillis(100));
         try {
            exito = wait.until(ExpectedConditions.visibilityOfElementLocated(testObject));
            conseguido = true;
         }
         catch (TimeoutException | StaleElementReferenceException e) {
            String mensaje = "Error al esperar que el objeto " + testObject.toString() + " sea visible";
            log.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return exito;
   }

   /**
    * Método que permite saber si el @param ha dejado de ser visible. En caso de que siga visible lanza un error.
    *
    * @param testObject
    * @throws PruebaAceptacionExcepcion
    */
   public void esperarHastaQueElementoNoSeaVisible(By testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperarHastaQueElementoNoSeaVisible->" + testObject.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
               Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
               Duration.ofMillis(100));
         try {
            wait.until(ExpectedConditions.invisibilityOf(WebDriverFactory.getDriver().findElement(testObject)));
            conseguido = true;
         }
         catch (TimeoutException | StaleElementReferenceException e) {
            String mensaje = "Error al esperar que el objeto " + testObject.toString() + " dejara de ser visible";
            log.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
   }

   public void esperarHastaQueElementoPresenteBreve(By testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperarHastaQueElementoPresenteBreve->" + testObject.toString());
      WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_CORTO))),
            Duration.ofMillis(100));
      try {
         wait.until(ExpectedConditions.presenceOfElementLocated(testObject));
      }
      catch (TimeoutException | StaleElementReferenceException e) {
         String mensaje = "Error al esperar brevemente que el objeto " + testObject + " esté presente";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public WebElement esperarHastaQueElementoPresente(By testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperarHastaQueElementoPresente->" + testObject.toString());
      WebElement exito = null;
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
               Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
               Duration.ofMillis(100));
         try {
            exito = wait.until(ExpectedConditions.presenceOfElementLocated(testObject));
            conseguido = true;
         }
         catch (TimeoutException | StaleElementReferenceException e) {
            String mensaje = "Error al esperar que el objeto " + testObject + " esté presente";
            log.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return exito;
   }

   public WebElement esperarHastaQueElementoClickable(WebElement testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperarHastaQueElementoClickable->" + testObject.getAttribute("id"));
      WebElement exito = null;
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
               Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
               Duration.ofMillis(100));
         try {
            exito = wait.until(ExpectedConditions.elementToBeClickable(testObject));
            conseguido = true;
         }
         catch (TimeoutException | StaleElementReferenceException e) {
            String mensaje = "Error al esperar que el objeto " + testObject + " sea clickable";
            log.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return exito;
   }

   public WebElement esperarHastaQueElementoClickable(By testObject) throws PruebaAceptacionExcepcion {
      log.trace("esperarHastaQueElementoClickable->" + testObject);
      WebElement exito = null;
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
               Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
               Duration.ofMillis(100));
         try {
            exito = wait.until(ExpectedConditions.elementToBeClickable(testObject));
            conseguido = true;
         }
         catch (TimeoutException | StaleElementReferenceException e) {
            String mensaje = "Error al esperar que el objeto " + testObject + " sea clickable";
            log.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return exito;
   }

   private boolean esperarHastaQueElementoTengaTexto(By testObject, String texto) throws PruebaAceptacionExcepcion {
      log.trace("esperarHastaQueElementoContengaTexto->" + testObject);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
               Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
               Duration.ofMillis(100));
         try {
            conseguido = wait.until(ExpectedConditions.textToBe(testObject, texto));
         }
         catch (TimeoutException | StaleElementReferenceException e) {
            String mensaje = "Error al esperar que el objeto " + testObject + " contenga el texto";
            log.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return conseguido;
   }

   /**
    * Resalta el @element de @param color, útil para seguir una traza visual.
    *
    * @param driver
    * @param element
    * @param color
    * @throws PruebaAceptacionExcepcion
    */
   private void resaltaObjeto(WebElement element, String color) {
      log.trace("resaltaObjeto->" + element.toString() + ". Color=" + color);
      try {
         JavascriptExecutor js = (JavascriptExecutor) WebDriverFactory.getDriver();
         js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element,
               "background: " + color + "; color: black; border: 3px solid black;");
      }
      catch (Exception e) {
         String mensaje = "El elemento " + element + " no puede ser resaltado con color " + color;
         log.error(mensaje);
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
      try {
         if (this.isObjetoPresente(testObject)) {
            WebElement elemento = this.esperaCompleta(testObject);

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

   public boolean isTextPresent(String texto) {
      log.debug("isTextPresent->" + texto);
      // Se va a intentar localizar un numero finito de veces, si no se encuentra en ninguna esas veces, se considera no esta
      boolean encontrado = false;
      for (int i = 1; !encontrado && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            // Intento i-esimo
            encontrado = this.encuentraTextoEnPagina(texto);
         }
         catch (Exception e) {
            log.error(e.getLocalizedMessage());
            // Porque no parece funcionar bien si no lo encuentra, aunque, segun la descripcion, deberia devolver false y pto...
            encontrado = false;
         }
      }
      return encontrado;
   }

   private void esperaCorta() {
      try {
         this.esperarDesaparezcaProcesando();
         Thread.sleep(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_CORTO)) * 1000);
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
      }
   }

   private void esperaMedia() {
      try {
         this.esperarDesaparezcaProcesando();
         Thread.sleep(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO)) * 1000);
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
      }
   }

   public void retrasoLargo() {
      try {
         this.esperarDesaparezcaProcesando();
         Thread.sleep(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_LARGO)) * 1000);
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
      }
   }

   // FIXME: Mover al proyecto cliente de Selenium
   public int obtieneNumeroResultadosIndicadoEnListado(String clase) throws PruebaAceptacionExcepcion {
      log.debug("obtieneNumeroResultadosIndicadoEnListado->" + clase);
      boolean conseguido = false;
      int num = 0;
      try {
         By objetoBuscado = By.xpath("//*[@class = '" + clase + "']");
         String texto = this.getText(objetoBuscado);
         if (texto.contains("Resultados: ")) {

            int indice = texto.indexOf(":");
            String subcadena = texto.substring(indice + 1).trim();
            num = Integer.parseInt(subcadena);
            conseguido = true;
         }
         if (!conseguido) {
            String mensaje =
                  "No existe un tal elemento 'paragraph' con clase: " + clase + "que contenga el numero de resultados del listado";
            log.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      catch (Exception e) {
         String mensaje = e.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return num;
   }

   /**
    * Pulsa sobre el unico elemento que debe existir en el listado que contenga en su titulo el parametro {@code titulo} y en su class el
    * parametro {@code clase}. Ademas ese elemento debe ser un 'tag = input' y de 'type = submit'. NOTA - esto es para pulsar sobre los
    * iconos de los listados y tener en un lugar centralizado lo que se debe hacer con lo que si cambia algo, se cambia el codigo de este
    * metodo de forma unificada, aunque dejen de tener sentido los nombres de los parametros.
    *
    * @param genericObjectPath
    *           path hacia el objeto generico a utilizar.
    * @param titulo
    *           a considerar para el title.
    * @param clase
    *           a considerar para la clase CSS.
    * @throws PruebaAceptacionExcepcion
    */
   public void pulsaUnicoElementoPara(String titulo, String clase) throws PruebaAceptacionExcepcion {
      log.debug("pulsaUnicoElementoPara->" + titulo + "-" + clase);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoParaAux(titulo, clase);
      }
      if (!conseguido) {
         String mensaje = "No existe un único elemento a cliquear en el listado";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Pulsa sobre el unico elemento que debe existir en el listado que contenga en su titulo el parametro {@code titulo} y en su class el
    * parametro {@code clase}. Ademas ese elemento debe ser un 'tag = input' y de 'type = submit'. NOTA - esto es para pulsar sobre los
    * iconos de los listados y tener en un lugar centralizado lo que se debe hacer con lo que si cambia algo, se cambia el codigo de este
    * metodo de forma unificada, aunque dejen de tener sentido los nombres de los parametros.
    *
    * @param genericObjectPath
    *           path hacia el objeto generico a utilizar.
    * @param titulo
    *           a considerar para el title.
    * @param clase
    *           a considerar para la clase CSS.
    * @return devuelve true si consigue pulsar sobre ese unico elemento; en caso contrario, devuelve false.
    */
   private boolean pulsaUnicoElementoParaAux(String titulo, String clase) {
      log.trace("pulsaUnicoElementoParaAux->" + titulo + "-" + clase);
      try {
         By objetoBuscado = By.xpath("//input[contains(@class, '" + clase + "') and contains(@title, '" + titulo + "')]");
         WebElement objetoEncontrado = this.esperaCompleta(objetoBuscado);
         this.resaltaObjeto(objetoEncontrado, COLOR_AMARILLO);
         objetoEncontrado.click();
         return true;
      }
      catch (Exception e) {
         log.debug(e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * Pulsa sobre el unico elemento que debe existir en el listado que contenga en su id el parametro {@code id}. Ademas ese elemento debe
    * ser un 'tag = input' y de 'type = submit'. NOTA - esto es para pulsar sobre los iconos de los listados y tener en un lugar
    * centralizado lo que se debe hacer con lo que si cambia algo, se cambia el codigo de este metodo de forma unificada, aunque dejen de
    * tener sentido los nombres de los parametros.
    *
    * @param genericObjectPath
    *           path hacia el objeto generico a utilizar.
    * @param id
    *           a considerar para el id.
    * @throws PruebaAceptacionExcepcion
    */
   public void pulsaUnicoElementoParaIdTitulo(String id, String titulo) throws PruebaAceptacionExcepcion {
      log.debug("pulsaUnicoElementoParaIdTitulo->" + id + ". Título->" + titulo);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoParaIdTituloAux(id, titulo);
      }
      if (!conseguido) {
         String mensaje = "No se ha podido pulsar el elemento con id que contiene la cadena " + id + " y el titulo " + titulo;
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Pulsa sobre el unico elemento que debe existir en el listado que contenga en su id el parametro {@code id}. Ademas ese elemento debe
    * ser un 'tag = input' y de 'type = submit'. NOTA - esto es para pulsar sobre los iconos de los listados y tener en un lugar
    * centralizado lo que se debe hacer con lo que si cambia algo, se cambia el codigo de este metodo de forma unificada, aunque dejen de
    * tener sentido los nombres de los parametros.
    *
    * @param genericObjectPath
    *           path hacia el objeto generico a utilizar.
    * @param id
    *           a considerar para el id.
    * @return devuelve true si consigue pulsar sobre ese unico elemento; en caso contrario, devuelve false.
    */
   private boolean pulsaUnicoElementoParaIdTituloAux(String id, String titulo) {
      log.trace("pulsaUnicoElementoParaIdTituloAux->" + id + ". Título->" + titulo);
      try {
         By objetoBuscado = By.xpath("//input[contains(@id, '" + id + "') and contains(@title, '" + titulo + "')]");
         return this.pulsaEnElUnico(objetoBuscado);
      }
      catch (Exception e) {
         log.debug(e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * Pulsa sobre el unico elemento que debe existir en el listado que contenga en su id el parametro {@code id}. Ademas ese elemento debe
    * ser un 'tag = input' y de 'type = submit'. NOTA - esto es para pulsar sobre los iconos de los listados y tener en un lugar
    * centralizado lo que se debe hacer con lo que si cambia algo, se cambia el codigo de este metodo de forma unificada, aunque dejen de
    * tener sentido los nombres de los parametros.
    *
    * @param genericObjectPath
    *           path hacia el objeto generico a utilizar.
    * @param id
    *           a considerar para el id.
    */
   public void pulsaUnicoElementoParaId(String id) throws PruebaAceptacionExcepcion {
      log.debug("pulsaUnicoElementoParaId->" + id);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoParaIdAux(id);
      }
      if (!conseguido) {
         String mensaje = "No se ha podido pulsar el elemento con id que contiene la cadena " + id;
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Pulsa sobre el unico elemento que debe existir en el listado que contenga en su id el parametro {@code id}. Ademas ese elemento debe
    * ser un 'tag = input' y de 'type = submit'. NOTA - esto es para pulsar sobre los iconos de los listados y tener en un lugar
    * centralizado lo que se debe hacer con lo que si cambia algo, se cambia el codigo de este metodo de forma unificada, aunque dejen de
    * tener sentido los nombres de los parametros.
    *
    * @param genericObjectPath
    *           path hacia el objeto generico a utilizar.
    * @param id
    *           a considerar para el id.
    * @return devuelve true si consigue pulsar sobre ese unico elemento; en caso contrario, devuelve false.
    */
   private boolean pulsaUnicoElementoParaIdAux(String id) {
      log.trace("pulsaUnicoElementoParaIdAux->" + id);
      try {
         By objetoBuscado = By.xpath("//input[contains(@id, '" + id + "')]");
         return this.pulsaEnElUnico(objetoBuscado);
      }
      catch (Exception e) {
         log.debug(e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * Busca el objeto {@code genericObject} y si solo existe él, le hace clic y hace una espera mínima.
    *
    * @param genericObject
    * @return true si localizó el objeto, era único y le hizo clic, false en caso contrario
    */
   private boolean pulsaEnElUnico(By objetoBuscado) {
      log.trace("pulsaEnElUnico->" + objetoBuscado);
      try {
         // Se espera a que desaparezca el mensaje procesando o se cargue la pagina...
         this.esperaCompleta(objetoBuscado);

         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);
         if (null != webElements && 1 == webElements.size()) {
            // Solo puede quedar UNO!!!
            WebElement elemento = webElements.get(0);
            elemento.click();
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            return true;
         }
      }
      catch (Exception e) {
         log.debug(e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * Devuelve el numero de botones visibles en el listado de una pagina que sea un listado, que verifican las condiciones indicadas.
    *
    * @param genericObjectPath
    *           path a objeto generico usado en la busqueda.
    * @param titulo
    *           el valor aqui indicado ha de estar incluido en el atributo 'title' de los elementos buscados.
    * @param clase
    *           el valor aqui indicado ha de estar incluido en el atributo 'class' de los elementos buscados.
    * @return el numero de botones visibles en el listado de una pagina que sea un listado, que verifican las condiciones indicadas.
    * @throws PruebaAceptacionExcepcion
    */
   public int obtieneNumeroBotonesVisiblesEnListadoPara(String titulo, String clase) throws PruebaAceptacionExcepcion {
      log.debug("obtieneNumeroBotonesVisiblesEnListadoPara->" + titulo + "-" + clase);
      try {
         int num = 0;

         // Se espera a que desaparezca el mensaje procesando o se cargue la pagina...
         this.esperaCorta();

         // Aqui no tiene sentido la repeticion de intentos pues, tras esperar la carga de la pagina, basta obtener el numero de botones que
         // se consideren.
         By objetoBuscado = By.xpath("//input[contains(@class, '" + clase + "') and contains(@title, '" + titulo + "')]");

         // List<WebElement> webElements = WebUiCommonHelper.findWebElements(genericObject, GlobalVariable.tiempoRetrasoCorto)
         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);

         if (null != webElements) {
            num = webElements.size();
         }
         return num;
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
         throw e;
      }
   }

   /**
    * Se intenta pulsar sobre el elemento i-esimo del listado. Si no se consigue se deja constancia de ello.
    *
    * @param genericObjectPath
    *           path a objeto generico utilizado para lleInformeListener a cabo esta funcionalidad.
    * @param pos
    *           posicion i-esima cuyo elemento se pretende pulsar, comenzando en 1, 2, 3, ... tam del listado.
    * @param titulo
    *           a considerar para el title.
    * @param clase
    *           a considerar para la clase CSS.
    * @throws PruebaAceptacionExcepcion
    */
   public void pulsaElementoIesimoPara(int pos, String titulo, String clase) throws PruebaAceptacionExcepcion {
      log.debug("pulsaElementoIesimoPara->" + pos + "-" + titulo + "-" + clase);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento siguiente
         conseguido = this.pulsaElementoIesimoParaAux(pos, titulo, clase);
      }
      if (!conseguido) {
         String mensaje = "No ha sido posible cliquear en elemento del listado de la posicion: " + pos;
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Se pulsa en el elemento i-esimo del listado de acuerdo a las condiciones indicadas.
    *
    * @param genericObjectPath
    *           path a objeto generico utilizado para lleInformeListener a cabo esta funcionalidad.
    * @param pos
    *           posicion i-esima cuyo elemento es pulsado, comenzando en 1, 2, 3, ... tam del listado.
    * @param titulo
    *           a considerar para el title.
    * @param clase
    *           a considerar para la clase CSS.
    * @return si se consigue pulsar sobre este elemento i-esimo o no.
    */
   private boolean pulsaElementoIesimoParaAux(int pos, String titulo, String clase) {
      log.trace("pulsaElementoIesimoParaAux->" + pos + "-" + titulo + "-" + clase);
      boolean pulsado = false;
      try {
         By objetoBuscado = By.xpath("//input[contains(@class, '" + clase + "') and contains(@title, '" + titulo + "')]");

         this.esperaCorta();

         // List<WebElement> webElements = WebUiCommonHelper.findWebElements(genericObject, GlobalVariable.tiempoRetrasoCorto)
         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);

         if (null != webElements && webElements.size() > 0 && webElements.size() >= pos) {
            log.trace("" + webElements.size());
            // Pulsamos el i-esimo
            this.resaltaObjeto(webElements.get(pos - 1), COLOR_AMARILLO);
            webElements.get(pos - 1).click();
            this.esperaCorta();

            pulsado = true;
         }
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
      }
      return pulsado;
   }

   public void pulsaPrimerElementoDocsAnexos(String textoEnlace) throws PruebaAceptacionExcepcion {
      log.debug("pulsaPrimerElementoDocsAnexos->" + textoEnlace);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaPrimerElementoDocsAnexosAux(textoEnlace);
      }
      if (!conseguido) {
         String mensaje = "No existe un primer tipo de documentos anexos a seleccionar";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Intenta pulsar sobre el primer elemento que se encuentra en la pagina que contiene el texto que se pasa como parametro.
    *
    * @param textoEnlace
    *           texto que aparece dentro del enlace.
    * @return Si consigue pulsar sobre el texto, devuelve 1; caso contrario, devuelve 0.
    */
   private boolean pulsaPrimerElementoDocsAnexosAux(String textoEnlace) {
      log.trace("pulsaPrimerElementoDocsAnexosAux->" + textoEnlace);

      try {
         this.esperaCorta();

         By objetoBuscado = By.xpath("//a[contains(text(), '" + textoEnlace + "')]");
         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);
         for (WebElement webElement : webElements) {
            // Si lo encuentra, pulsa sobre el y punto..., devolviendo un 1 como que lo pulso
            this.resaltaObjeto(webElement, COLOR_AMARILLO);
            webElement.click();
            return true;
         }
      }
      catch (Exception e) {
         log.debug(e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * Se hace click sobre el icono iesimo de subida de un fichero dentro del listado de documentos presentes en la pagina mostrada.
    *
    * @param rutaFichero
    *           ruta hacia el fichero que va a ser subido a la aplicacion.
    * @param posicion
    *           posicion en el listado de iconos de subida que indica el icono sobre el que se va a pulsar.
    * @throws PruebaAceptacionExcepcion
    */
   public void clickParaUploadFicheroIesimoListado(String rutaFichero, int posicion) throws PruebaAceptacionExcepcion {
      log.debug("clickParaUploadFicheroIesimoListado->" + rutaFichero + "-" + posicion);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.clickParaUploadFicheroIesimoListadoAux(rutaFichero, posicion);
      }
      if (!conseguido) {
         String mensaje = "No se puede hacer click en elemento i-esimo del listado: posicion = " + posicion;
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Se hace click sobre el icono iesimo de subida de un fichero dentro del listado de documentos presentes en la pagina mostrada.
    *
    * @param rutaFichero
    *           ruta hacia el fichero que va a ser subido a la aplicacion.
    * @param posicion
    *           posicion en el listado de iconos de subida que indica el icono sobre el que se va a pulsar.
    * @return devuelve si ha sido posible o no pulsar sobre dicho icono en dicha posicion.
    */
   private boolean clickParaUploadFicheroIesimoListadoAux(String rutaFichero, int posicion) {
      log.trace("clickParaUploadFicheroIesimoListadoAux->" + rutaFichero + "-" + posicion);
      try {
         String id = ":" + (posicion - 1) + ":subirFichero:file";
         By objetoBuscado = By.xpath("//input[@type = 'file' and contains(@id, '" + id + "')]");

         // this.esperaCorta();
         // List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);
         // if (null != webElements && 1 == webElements.size()) {
         // // La posicion es posible...
         // WebElement elemento = webElements.get(0);
         // this.resaltaObjeto(elemento, COLOR_AMARILLO);
         // this.esperarHastaQueElementoClickable(elemento).click();
         // elemento.sendKeys(rutaFichero);
         // return true;
         // }
         WebElement elemento = this.esperaParaSubirFichero(objetoBuscado);
         elemento.sendKeys(rutaFichero);
         return true;
      }
      catch (Exception e) {
         log.error("No se puede hacer clic en el fichero en posición: " + posicion + ". " + e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * Pulsa sobre el unico elemento descargable en el listado de documentacion anexa.
    *
    * @throws PruebaAceptacionExcepcion
    */
   public void pulsaUnicoElementoDescargable() throws PruebaAceptacionExcepcion {
      log.debug("pulsaUnicoElementoDescargable");
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoDescargableAux();
      }
      if (!conseguido) {
         String mensaje = "No existe un unico elemento descargable en el listado";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Intenta pulsar sobre el unico elemento descargable.
    *
    * @return Si consigue pulsar sobre el unico elemento descargable, devuelve 1; caso contrario, devuelve 0.
    */
   private boolean pulsaUnicoElementoDescargableAux() {
      log.trace("pulsaUnicoElementoDescargableAux");
      try {
         By objetoBuscado = By.xpath("//input[@type = 'submit' and contains(@id, ':cLDescargaFichero')]");

         this.esperaCorta();

         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);
         if (null != webElements && 1 == webElements.size()) {
            // SOLO debe quedar UNO!!!
            WebElement elemento = webElements.get(0);
            // webElements.get(0).click();
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            this.esperarHastaQueElementoClickable(elemento).click();
            return true;
         }
      }
      catch (Exception e) {
         log.error("No se puede hacer clic en el único elemento descargable: " + e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * Pulsa sobre el único elemento a eliminar en el listado de documentación anexa.
    *
    * @throws PruebaAceptacionExcepcion
    */
   public void pulsaUnicoElementoEliminable() throws PruebaAceptacionExcepcion {
      log.debug("pulsaUnicoElementoEliminable");
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoEliminableAux();
      }
      if (!conseguido) {
         String mensaje = "No se ha podido pulsar en el único elemento eliminable";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Intenta pulsar sobre el unico elemento a eliminar en el listado de documentacion anexa.
    *
    * @return Si consigue pulsar sobre el unico elemento a eliminar, devuelve 1; caso contrario, devuelve 0.
    */
   private boolean pulsaUnicoElementoEliminableAux() {
      log.trace("pulsaUnicoElementoEliminableAux");
      try {
         By objetoBuscado = By.xpath("//input[contains(@id, ':aJCBEliminarFichero')]");

         this.esperaCorta();

         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);
         if (null != webElements && 1 == webElements.size()) {
            // Solo puede quedar UNO!!!
            WebElement elemento = webElements.get(0);
            // webElements.get(0).click()
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            this.esperarHastaQueElementoClickable(elemento).click();
            return true;
         }
      }
      catch (Exception e) {
         log.error("No se puede hacer clic en el único elemento eliminable: " + e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * Se hace click cambiando el foco inmediatamente, para no dar tiempo a elemento alguno a suceder posteriormente.
    *
    * @param testObject
    *           elmento sobre el que se hace clic.
    * @return no se devuelve nada.
    * @throws PruebaAceptacionExcepcion
    */
   public void clickCambiandoFoco(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("clickCambiandoFoco->" + testObject.toString());
      try {
         this.click(testObject);
      }
      catch (PruebaAceptacionExcepcion e) {
         log.error(e.getLocalizedMessage());
         throw e;
      }
   }

   public WebElement clickConUrlAfirmaProtocol(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("clickConUrlAfirmaProtocol->" + testObject.toString());
      boolean conseguido = false;
      WebElement elemento = null;
      Exception excepcion = null;
      // String window = WebDriverFactory.getDriver().getWindowHandle();
      this.ejecutaAccionesUrlAfirmaProtocol();
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaCompleta(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            WebElement elementoClickable = this.esperarHastaQueElementoClickable(elemento);
            if (elementoClickable != null) {
               elementoClickable.click();
               conseguido = true;
            }
            else {
            }
         }
         catch (Exception e) {
            log.error(e.getLocalizedMessage());
            // WebDriverFactory.getDriver().switchTo().window(window);
            // this.cambiarFoco();
            this.ejecutaAccionesUrlAfirmaProtocol();

            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje =
               "No se puede hacer click tras esperar el URL Afirma Protocol. Motivo del error: " + excepcion.getLocalizedMessage();
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return elemento;
   }

   private void ejecutaAccionesUrlAfirmaProtocol() throws PruebaAceptacionExcepcion {
      try {
         Robot rb = new Robot();

         this.esperaMedia();

         log.debug("Pulsar <DERECHA>");
         rb.keyPress(KeyEvent.VK_RIGHT);
         rb.keyRelease(KeyEvent.VK_RIGHT);
         log.debug("Soltar <DERECHA>");
         log.debug("Pulsar <INTRO>");
         rb.keyPress(KeyEvent.VK_ENTER);
         rb.keyRelease(KeyEvent.VK_ENTER);
         log.debug("Soltar <INTRO>");
      }
      catch (AWTException e) {
         String mensaje = "Error al manejar el robot";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   // private void cambiarFoco() throws PruebaAceptacionExcepcion {
   // try {
   // Robot rb = new Robot();
   // log.debug("Cambiar el foco a Autofirma");
   // rb.keyPress(KeyEvent.VK_ALT);
   // rb.keyPress(KeyEvent.VK_TAB);
   // rb.delay(10);
   // rb.keyRelease(KeyEvent.VK_ALT);
   // rb.keyRelease(KeyEvent.VK_TAB);
   // rb.delay(1000);
   // }
   // catch (AWTException e) {
   // String mensaje = "Error al manejar el robot";
   // log.error(mensaje);
   // throw new PruebaAceptacionExcepcion(mensaje);
   // }
   // }

   /**
    * Se intenta pulsar sobre el primer elemento del listado. Si no se consigue se deja constancia de ello.
    *
    * @param genericObjectPath
    *           path a objeto generico utilizado para lleInformeListener a cabo esta funcionalidad.
    * @param titulo
    *           a considerar para el title.
    * @param clase
    *           a considerar para la clase CSS.
    * @throws PruebaAceptacionExcepcion
    */
   public void pulsaPrimerElementoPara(String titulo, String clase) throws PruebaAceptacionExcepcion {
      log.debug("pulsaPrimerElementoPara->" + titulo + "-" + clase);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaPrimerElementoParaAux(titulo, clase);
      }
      if (!conseguido) {
         String mensaje = "No ha sido posible cliquear en el primer elemento del listado";
         log.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Se pulsa el primer elemento del listado de acuerdo a las condiciones indicadas.
    *
    * @param genericObjectPath
    *           path a objeto generico utilizado para lleInformeListener a cabo esta funcionalidad.
    * @param titulo
    *           a considerar para el title.
    * @param clase
    *           a considerar para la clase CSS.
    * @return si se consigue pulsar sobre este primer elemento o no.
    */
   private boolean pulsaPrimerElementoParaAux(String titulo, String clase) {
      log.trace("pulsaPrimerElementoParaAux->" + titulo + "-" + clase);
      try {
         By objetoBuscado = By.xpath("//input[contains(@class, '" + clase + "') and contains(@title, '" + titulo + "')]");
         WebElement objetoEncontrado = this.esperaCompleta(objetoBuscado);
         this.resaltaObjeto(objetoEncontrado, COLOR_AMARILLO);
         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);
         if (null != webElements && webElements.size() > 0) {
            // Pulsamos el primero
            webElements.get(0).click();
            return true;
         }
      }
      catch (Exception e) {
         log.error(e.getLocalizedMessage());
      }
      return false;
   }

   /**
    * Hace click sobre el objeto que contenga el id pasado por parámetro. Para ello, crea un objeto genérico con el id como propiedad
    */
   public void clickGenerico(String id) throws PruebaAceptacionExcepcion {
      log.debug("clickGenerico->" + id);
      TestObject genericObject = new TestObject();
      genericObject.addProperty("id", ConditionType.CONTAINS, id);
      this.click(this.convertirXpath(genericObject));
   }

   public void clickMensajePorTexto(String texto) throws PruebaAceptacionExcepcion {
      log.debug("clickMensajePorTexto->" + texto);
      TestObject genericObject = new TestObject();
      genericObject.addProperty("tag", ConditionType.EQUALS, "p");
      genericObject.addProperty("text", ConditionType.CONTAINS, texto);
      this.click(this.convertirXpath(genericObject));
   }

   public void clickNodoArbol(String id) throws PruebaAceptacionExcepcion {
      log.debug("clickNodoArbol->" + id);
      TestObject genericObject = new TestObject();
      genericObject.addProperty("id", ConditionType.CONTAINS, id);
      genericObject.addProperty("class", ConditionType.CONTAINS, "icon-collapsed");
      this.click(this.convertirXpath(genericObject));
   }

   public By convertirXpath(TestObject genericObject) {
      log.trace("convertirXpath->" + genericObject);
      XPathBuilder xpath = new XPathBuilder(genericObject.getProperties());
      return By.xpath(xpath.build());
   }

   public boolean isElementoClickable(By testObject) throws PruebaAceptacionExcepcion {
      log.debug("isElementoClickable->" + testObject);
      WebElement elemento = null;
      try {
         WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
               Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_CORTO))),
               Duration.ofMillis(100));
         elemento = wait.until(ExpectedConditions.elementToBeClickable(testObject));
      }
      catch (TimeoutException | StaleElementReferenceException e) {
         String mensaje = "El objeto " + testObject + " no es clickable";
         log.trace(mensaje);
      }
      catch (PruebaAceptacionExcepcion e) {
         log.error(e.getLocalizedMessage());
         throw e;
      }
      return elemento != null;
   }
}