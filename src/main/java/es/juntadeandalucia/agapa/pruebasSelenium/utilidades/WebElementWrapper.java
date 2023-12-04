package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
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
import org.openqa.selenium.WebDriverException;
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

   private void debug(String mensaje) {
      log.debug(mensaje);
      WebDriverFactory.getLogger().log(Status.INFO, mensaje);
   }

   private void trace(String mensaje) {
      if (log.isTraceEnabled()) {
         log.trace(mensaje);
         WebDriverFactory.getLogger().log(Status.INFO, MarkupHelper.createLabel(mensaje, ExtentColor.GREY));
      }
   }

   private void warning(String mensaje) {
      log.warn(mensaje);
      WebDriverFactory.getLogger().warning(mensaje);
   }

   private void error(String mensaje) {
      log.error(mensaje);
      WebDriverFactory.getLogger().fail(mensaje);
   }

   private void error(Throwable t) {
      log.error(t.getLocalizedMessage());
      WebDriverFactory.getLogger().log(Status.FAIL, t);
   }

   private String mensajeDeError(Exception e) {
      if (e instanceof WebDriverException) {
         return ((WebDriverException) e).getRawMessage();
      }
      return e.getLocalizedMessage();
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
      this.debug("click->" + testObject.toString());
      boolean conseguido = false;
      WebElement elemento = null;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elementoEsperado = this.esperaCompleta(testObject);
            this.resaltaObjeto(elementoEsperado, COLOR_AMARILLO);
            elemento = this.esperarHastaQueElementoClickable(elementoEsperado);
            elemento.click();
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al hacer click en el elemento";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return elemento;
   }

   public void doubleClick(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("doubleClick->" + testObject.toString());
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al hacer click en el elemento";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void escribeTexto(By testObject, String texto) throws PruebaAceptacionExcepcion {
      this.debug("escribeTexto->" + testObject.toString() + ". Texto=" + texto);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.click(testObject);
            String valor = elemento.getAttribute("value");
            if (valor != null && valor.length() > 0) {
               // Para borrar campos que tienen mucho texto y así sea mas rápido
               elemento.clear();
               elemento = this.esperaCompleta(testObject);
            }
            valor = elemento.getAttribute("value");
            if (valor != null && valor.length() > 0) {
               // Para borrar campos numéricos formateados con deciamles
               while (elemento.getAttribute("value").length() > 0) {
                  // Necesario porque así se soluciona donde aparece el cursor al hacer click (izq o der)
                  elemento.sendKeys(Keys.DELETE.toString());
                  elemento.sendKeys(Keys.BACK_SPACE.toString());
               }
               elemento = this.esperaCompleta(testObject);
            }
            this.asignaTexto(elemento, texto);
            elemento.sendKeys(Keys.TAB.toString()); // Hay veces que si no se pulsa TAB, no funciona
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al escribir texto en " + testObject.toString();
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   private void asignaTexto(WebElement elemento, String texto) {
      this.trace("asignaTexto->" + elemento.toString() + ". texto=" + texto);
      try {
         if (texto.length() > 1) {
            elemento.sendKeys(texto.substring(0, 1));
         }
         JavascriptExecutor js = (JavascriptExecutor) WebDriverFactory.getDriver();
         js.executeScript("arguments[0].value = arguments[1];", elemento, texto);
         // for (int x = 0; x < texto.length(); x++) {
         // elemento.sendKeys(texto.substring(x, x + 1));
         // }
      }
      catch (Exception e) {
         String mensaje = "No se puede escribir " + texto + " en " + elemento.toString() + ". Motivo: " + this.mensajeDeError(e);
         this.warning(mensaje);
      }
   }

   /**
    * Escribe un texto tecleando caracter por caracter en el campo sugerencia, y pulsa el caracter de "down arrow" y a continuacion "enter".
    */
   public void escibeTextoEnSugerencia(By testObject, String texto) throws PruebaAceptacionExcepcion {
      this.debug("escibeTextoEnSugerencia->" + testObject.toString() + ". Texto=" + texto);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {

         try {
            WebElement elemento = this.click(testObject);
            while (elemento.getAttribute("value").length() > 0) {
               // necesario porque así se soluciona donde aparece el cursor al hacer click (izq o der)
               elemento.sendKeys(Keys.DELETE.toString());
               elemento.sendKeys(Keys.BACK_SPACE.toString());
            }
            for (int x = 0; x < texto.length(); x++) {
               elemento.sendKeys(texto.substring(x, x + 1));
            }
            elemento.sendKeys(Keys.DOWN.toString());
            elemento.sendKeys(Keys.ENTER.toString());
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "No se puede escribir el texto " + texto + " en el campo sugerencia " + testObject.toString();
         this.error(excepcion);
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void subirFichero(By testObject, String ruta) throws PruebaAceptacionExcepcion {
      this.debug("subirFichero->" + testObject.toString() + ". Texto=" + ruta);
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
      // error(mensaje);
      // throw new PruebaAceptacionExcepcion(mensaje);
      // }
   }

   public void selectOptionByIndex(By testObject, Integer index) throws PruebaAceptacionExcepcion {
      this.debug("selectOptionByIndex->" + testObject.toString() + ". Index=" + index);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            // elemento = this.esperarHastaQueElementoClickable(elemento);

            Select comboBox = new Select(elemento);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            comboBox.selectByIndex(index);
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al hacer seleccionar el valor del combo por índice";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
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
      this.debug("selectOptionByLabel->" + testObject.toString() + ". Label=" + label);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            // elemento = this.esperarHastaQueElementoClickable(elemento);

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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al hacer seleccionar el valor del combo por etiqueta";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void selectOptionByLabel(By testObject, String label, boolean conRetraso) throws PruebaAceptacionExcepcion {
      this.debug("selectOptionByLabel->" + testObject.toString() + ". Label=" + label + ". Con retraso=" + conRetraso);
      try {
         this.selectOptionByLabel(testObject, label);
         if (conRetraso) {
            this.esperaCorta();
         }
      }
      catch (PruebaAceptacionExcepcion e) {
         String mensaje = "Error en " + testObject.toString()
               + " al hacer seleccionar el valor del combo por etiqueta con retraso. Motivo del error: " + this.mensajeDeError(e);
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyOptionSelectedByLabel(By testObject, String label) throws PruebaAceptacionExcepcion {
      this.debug("verifyOptionSelectedByLabel->" + testObject.toString() + ". Label=" + label);
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error  en " + testObject.toString() + "al hacer seleccionar el valor del combo por etiqueta";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      else if (!label.equals(etiquetaSeleccionada)) {
         String mensaje = "En el combo no está seleccionado con el valor " + label;
         this.error(mensaje);
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
      this.debug("verifyOptionPresentByLabel->" + testObject.toString() + ". Label=" + label);
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "La opción " + label + " no está presente en " + testObject.toString();
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
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
      this.debug("verifyOptionNotPresentByLabel->" + testObject.toString() + ". Label=" + label);
      boolean conseguido = false;
      boolean presente = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            Select comboBox = new Select(elemento);
            conseguido = true;
            for (WebElement opcion : comboBox.getOptions()) {
               if (opcion.getText().equals(label)) {
                  presente = true;
                  break;
               }
            }
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Elemento " + testObject.toString() + " no encontrado";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      else if (presente) {
         String mensaje = "La opción " + label + " está presente cuando no debería en " + testObject.toString();
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void selectOptionByValue(By testObject, String value) throws PruebaAceptacionExcepcion {
      this.debug("selectOptionByValue->" + testObject.toString() + ". Value=" + value);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            // elemento = this.esperarHastaQueElementoClickable(elemento);

            Select comboBox = new Select(elemento);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            comboBox.selectByValue(value);
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al hacer seleccionar el valor del combo por value del option";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public String obtieneValorSeleccionadoEnCombo(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("obtieneValorSeleccionadoEnCombo->" + testObject.toString());
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al hacer seleccionar el valor del combo por value del option";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return valorSeleccionado;
   }

   public void verifyElementText(By testObject, String text) throws PruebaAceptacionExcepcion {
      this.debug("verifyElementText->" + testObject.toString() + ". Text=" + text);
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al verificar el texto del elemento " + testObject.toString()
               + ". Texto esperado: " + text;
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
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
      this.debug("waitUntilElementTextChangedOrDisapeared->" + testObject.toString() + ". Text=" + text);
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
               this.resaltaObjeto(elemento, COLOR_AZUL);
            }
         }
         catch (Exception e) {
            this.error(this.mensajeDeError(e));
         }
      }
   }

   public void verifyElementPresent(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("verifyElementPresent->" + testObject.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         if (this.isObjetoPresente(testObject)) {
            conseguido = true;
         }
      }
      if (!conseguido) {
         String mensaje = "El objeto " + testObject.toString() + " no está presente";
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementNotPresent(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("verifyElementNotPresent->" + testObject.toString());
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
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementChecked(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("verifyElementChecked->" + testObject.toString());
      boolean conseguido = this.isElementChecked(testObject);
      if (!conseguido) {
         String mensaje = "El objeto " + testObject.toString() + " no está checkeado cuando debería";
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementNotChecked(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("verifyElementNotChecked->" + testObject.toString());
      boolean marcado = this.isElementChecked(testObject);
      if (marcado) {
         String mensaje = "El objeto " + testObject.toString() + " está checkeado cuando no debería";
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public String getAttribute(By testObject, String atributo) throws PruebaAceptacionExcepcion {
      this.debug("getAttribute->" + testObject.toString() + ". Atributo=" + atributo);
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al obtener el atributo " + atributo;
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return valorAtributo;
   }

   public void verifyElementAttributeValue(By testObject, String atributo, String value) throws PruebaAceptacionExcepcion {
      this.debug("verifyElementAttributeValue->" + testObject.toString() + ". Atributo=" + atributo + ". Value=" + value);
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al obtener el atributo del elemento web";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      else if (!value.equals(valorAtributo)) {
         String mensaje = "El valor " + value + " del atributo " + atributo + " no es el esperado (" + valorAtributo + ")";
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyTextPresent(String texto) throws PruebaAceptacionExcepcion {
      this.debug("verifyTextPresent->" + texto);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.encuentraTextoEnPagina(texto);
      }
      if (!conseguido) {
         String mensaje = "Texto " + texto + " no presente";
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   private boolean encuentraTextoEnPagina(String texto) {
      boolean conseguido = false;
      try {
         this.esperaCorta();
         WebElement textDemo = WebDriverFactory.getDriver().findElement(By.xpath("//*[contains(text(),'" + texto + "')]"));
         if (textDemo.isDisplayed()) {
            this.resaltaObjeto(textDemo, COLOR_AZUL);
            conseguido = true;
         }
      }
      catch (Exception e) {
         this.error(this.mensajeDeError(e));
      }
      return conseguido;
   }

   public void clickParaUploadFichero(By testObject, String rutaFichero) throws PruebaAceptacionExcepcion {
      this.debug("clickParaUploadFichero->" + testObject.toString() + ". RutaFichero=" + rutaFichero);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaParaSubirFichero(testObject);
            elemento.sendKeys(rutaFichero);
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al hacer click para subir fichero en el elemento";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   private WebElement esperaParaSubirFichero(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperaParaSubirFichero->" + testObject.toString());
      this.esperarDesaparezcaProcesando();
      WebElement elemento = this.esperarHastaQueElementoPresente(testObject);
      this.resaltaObjeto(elemento, COLOR_AMARILLO);
      return elemento;
   }

   public String getText(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("getText->" + testObject.toString());
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al obtener el texto del elemento";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
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
      this.debug("cuentaNumeroDeOpcionesEnSelect->" + testObject.toString());
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al hacer seleccionar el valor del combo por índice";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
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
      this.debug("obtenerFilaConTextoEnTabla->" + testObject.toString() + ". Texto=" + texto);
      int fila = -1;
      boolean conseguido = false;
      WebElement table = null;
      Exception excepcion = null;

      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            // Localizar el body de la tabla
            table = this.esperaCompleta(testObject);
            // Se obtienen todas las filas de la tabla
            List<WebElement> rows = table.findElements(By.xpath("//tr[contains(@class, 'rich-table-row')]"));
            // Se recorren todas las filas de la tabla
            for (int x = 0; !conseguido && x < rows.size(); x++) {
               // Se recorre columna por columna buscando el texto esperado
               List<WebElement> cols = rows.get(x).findElements(By.tagName("td"));
               for (WebElement col : cols) {
                  if (col.getText().trim().equalsIgnoreCase(texto)) {
                     fila = x;
                     conseguido = true;
                     this.resaltaObjeto(col, COLOR_AZUL);
                     break;
                  }
               }
            }
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (table == null) {
         String mensaje = "Error al obtener el id del cuerpo de la tabla";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      else if (fila == -1) {
         String mensaje = "No se encuentra la fila de la tabla con idBodyTabla: " + table.getAttribute("id") + " y texto buscado: " + texto;
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return fila;
   }

   public int obtieneNumeroDeFilasListado(String idBodyTabla) throws PruebaAceptacionExcepcion {
      this.debug("obtieneNumeroDeFilasListado->" + idBodyTabla);
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
            List<WebElement> rows = table.findElements(By.xpath("//table[@id='" + idBodyTabla + "']/tbody/tr"));
            // +"//tr[contains(@class, 'rich-table-row')]"));
            numeroDeFilas = rows.size();
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }

      if (!conseguido) {
         String mensaje = "Error al hacer seleccionar el valor del combo por índice";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return numeroDeFilas;
   }

   public void esperarFilaUnicaListado(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("esperarFilaUnicaListado->" + testObject.toString());
      this.esperarListadoConFilas(testObject, 1);
   }

   public void esperarListadoConFilas(By testObject, int numeroFilasEsperadas) throws PruebaAceptacionExcepcion {
      this.debug("esperarListadoConFilas->" + testObject.toString() + ". NumeroFilasEsperadas=" + numeroFilasEsperadas);
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
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al hacer seleccionar el valor del combo por índice";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void esperaIncondicional(int segundos) {
      this.debug("esperaIncondicional-> " + segundos + " segundos");
      try {
         Thread.sleep(segundos * 1000);
      }
      catch (InterruptedException e) {
         // Seguramente nunca se produzca esta excepción
         this.error("Error en espera activa");
      }
   }

   /**
    * Posicionar en porsición más alta de la página
    */
   public void scrollTopPagina() {
      this.debug("scrollTopPagina");
      ((JavascriptExecutor) WebDriverFactory.getDriver()).executeScript("window.scrollTo(0, -document.body.scrollHeight)");
   }

   private boolean esperarHastaQueElementoNoPresente(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("esperarHastaQueElementoNoPresente->" + testObject.toString());
      boolean conseguido = false;
      WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_LARGO))),
            Duration.ofMillis(100));
      try {
         conseguido = wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(testObject)));
      }
      catch (TimeoutException | StaleElementReferenceException e) {
         String mensaje = "Error al esperar que el objeto " + testObject.toString() + " NO esté presente";
         this.warning(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return conseguido;
   }

   public boolean isElementChecked(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("isElementChecked->" + testObject.toString());
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
            this.error(this.mensajeDeError(e));
         }
      }
      if (!conseguido) {
         StringBuilder mensaje = new StringBuilder("El objeto ").append(testObject).append(" NO está chequeado.");
         if (excepcion != null) {
            mensaje.append(". Motivo del error: ").append(this.mensajeDeError(excepcion));
         }
         this.error(mensaje.toString());
      }
      return marcado;
   }

   public boolean isObjetoPresente(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("isObjetoPresente->" + testObject.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MINIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaBreve(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            conseguido = false;
         }
      }
      if (!conseguido) {
         String mensaje = "El objeto " + testObject.toString() + " NO está presente";
         this.debug(mensaje);
      }
      return conseguido;
   }

   private void esperarDesaparezcaProcesando() throws PruebaAceptacionExcepcion {
      this.trace("esperarDesaparezcaProcesando");

      String idElementoProcesando = null;
      try {
         idElementoProcesando = VariablesGlobalesTest.getPropiedad(PropiedadesTest.ID_ELEMENTO_PROCESANDO);
      }
      catch (PruebaAceptacionExcepcion e) {
         this.warning("ID_ELEMENTO_PROCESANDO no definido en fichero properties");
      }
      if (idElementoProcesando != null) {
         By by = By.id(idElementoProcesando);
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
                     this.error("Error al parar el procesamiento del hilo de ejecución");
                  }
                  tiempo += 100;
                  if (tiempo > Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO)) * 1000) {
                     String mensaje = "La ventana \"Procesando...\" no desaparece";
                     this.error(mensaje);
                     throw new PruebaAceptacionExcepcion(mensaje);
                  }
               }
            }
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            this.trace("La ventana procesando ya no está visible");
         }
      }
   }

   private WebElement esperaBreve(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperaBreve->" + testObject.toString());
      this.esperarDesaparezcaProcesando();
      this.esperarHastaQueElementoPresenteBreve(testObject);
      return this.esperarHastaQueElementoVisibleBreve(testObject);
   }

   public WebElement esperaCompleta(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperaCompleta->" + testObject.toString());
      this.esperarDesaparezcaProcesando();
      this.esperarHastaQueElementoPresente(testObject);
      return this.esperarHastaQueElementoVisible(testObject);
   }

   private WebElement esperarHastaQueElementoVisibleBreve(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoVisibleBreve->" + testObject.toString());
      WebElement elemento = null;
      WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
            Duration.ofMillis(100));
      try {
         elemento = wait.until(ExpectedConditions.visibilityOfElementLocated(testObject));
      }
      catch (TimeoutException | StaleElementReferenceException e) {
         String mensaje = "Error al esperar brevemente que el objeto " + testObject.toString() + " sea visible";
         this.warning(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return elemento;
   }

   private WebElement esperarHastaQueElementoVisible(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoVisible->" + testObject.toString());
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
            this.warning(mensaje);
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
   private void esperarHastaQueElementoNoSeaVisible(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoNoSeaVisible->" + testObject.toString());
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
            this.warning(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
   }

   private void esperarHastaQueElementoPresenteBreve(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoPresenteBreve->" + testObject.toString());
      WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_CORTO))),
            Duration.ofMillis(100));
      try {
         wait.until(ExpectedConditions.presenceOfElementLocated(testObject));
      }
      catch (TimeoutException | StaleElementReferenceException e) {
         String mensaje = "Error al esperar brevemente que el objeto " + testObject.toString() + " esté presente";
         this.warning(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   private WebElement esperarHastaQueElementoPresente(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoPresente->" + testObject.toString());
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
            String mensaje = "Error al esperar que el objeto " + testObject.toString() + " esté presente";
            this.warning(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return exito;
   }

   private WebElement esperarHastaQueElementoClickable(WebElement testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoClickable->" + testObject.getAttribute("id"));
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
            String mensaje = "Error al esperar que el objeto " + testObject.toString() + " sea clickable";
            this.warning(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return exito;
   }

   public WebElement esperarHastaQueElementoClickable(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoClickable->" + testObject);
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
            String mensaje = "Error al esperar que el objeto " + testObject.toString() + " sea clickable";
            this.error(e);
            this.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return exito;
   }

   private boolean esperarHastaQueElementoTengaTexto(By testObject, String texto) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoContengaTexto->" + testObject);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
               Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO))),
               Duration.ofMillis(100));
         try {
            conseguido = wait.until(ExpectedConditions.textToBe(testObject, texto));
         }
         catch (TimeoutException | StaleElementReferenceException e) {
            String mensaje = "Error al esperar que el objeto " + testObject.toString() + " contenga el texto";
            this.warning(mensaje);
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
      this.trace("resaltaObjeto->" + element.toString() + ". Color=" + color);
      try {
         JavascriptExecutor js = (JavascriptExecutor) WebDriverFactory.getDriver();
         js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element,
               "background: " + color + "; color: black; border: 3px solid black;");
      }
      catch (Exception e) {
         String mensaje = "El elemento " + element + " no puede ser resaltado con color " + color;
         this.warning(mensaje);
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
      this.debug("obtenerTextoElemento->" + testObject.toString());
      try {
         if (this.isObjetoPresente(testObject)) {
            WebElement elemento = this.esperaCompleta(testObject);

            this.resaltaObjeto(elemento, COLOR_AZUL);
            return elemento.getText();
         }
      }
      catch (PruebaAceptacionExcepcion e) {
         String mensaje = "Error al verificar el texto del elemento. Motivo del error: " + this.mensajeDeError(e);
         this.error(e);
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return "";
   }

   public boolean isTextPresent(String texto) {
      this.debug("isTextPresent->" + texto);
      // Se va a intentar localizar un numero finito de veces, si no se encuentra en ninguna esas veces, se considera no esta
      boolean encontrado = false;
      for (int i = 1; !encontrado && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            // Intento i-esimo
            encontrado = this.encuentraTextoEnPagina(texto);
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
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
         this.warning(this.mensajeDeError(e));
      }
   }

   private void esperaMedia() {
      try {
         this.esperarDesaparezcaProcesando();
         Thread.sleep(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO)) * 1000);
      }
      catch (Exception e) {
         this.warning(this.mensajeDeError(e));
      }
   }

   public void retrasoLargo() {
      try {
         this.esperarDesaparezcaProcesando();
         Thread.sleep(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_LARGO)) * 1000);
      }
      catch (Exception e) {
         this.error(this.mensajeDeError(e));
      }
   }

   // FIXME: Mover al proyecto cliente de Selenium
   public int obtieneNumeroResultadosIndicadoEnListado(String clase) throws PruebaAceptacionExcepcion {
      this.debug("obtieneNumeroResultadosIndicadoEnListado->" + clase);
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
            this.error(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      catch (Exception e) {
         String mensaje = this.mensajeDeError(e);
         this.error(e);
         this.error(mensaje);
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
      this.debug("pulsaUnicoElementoPara->" + titulo + "-" + clase);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoParaAux(titulo, clase);
      }
      if (!conseguido) {
         String mensaje = "No existe un único elemento a cliquear en el listado";
         this.error(mensaje);
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
      this.trace("pulsaUnicoElementoParaAux->" + titulo + "-" + clase);
      try {
         By objetoBuscado = By.xpath("//input[contains(@class, '" + clase + "') and contains(@title, '" + titulo + "')]");
         WebElement objetoEncontrado = this.esperaCompleta(objetoBuscado);
         this.resaltaObjeto(objetoEncontrado, COLOR_AMARILLO);
         objetoEncontrado.click();
         return true;
      }
      catch (Exception e) {
         this.warning(this.mensajeDeError(e));
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
      this.debug("pulsaUnicoElementoParaIdTitulo->" + id + ". Título->" + titulo);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoParaIdTituloAux(id, titulo);
      }
      if (!conseguido) {
         String mensaje = "No se ha podido pulsar el elemento con id que contiene la cadena " + id + " y el titulo " + titulo;
         this.error(mensaje);
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
      this.trace("pulsaUnicoElementoParaIdTituloAux->" + id + ". Título->" + titulo);
      try {
         By objetoBuscado = By.xpath("//input[contains(@id, '" + id + "') and contains(@title, '" + titulo + "')]");
         return this.pulsaEnElUnico(objetoBuscado);
      }
      catch (Exception e) {
         this.warning(this.mensajeDeError(e));
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
      this.debug("pulsaUnicoElementoParaId->" + id);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoParaIdAux(id);
      }
      if (!conseguido) {
         String mensaje = "No se ha podido pulsar el elemento con id que contiene la cadena " + id;
         this.error(mensaje);
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
      this.trace("pulsaUnicoElementoParaIdAux->" + id);
      try {
         By objetoBuscado = By.xpath("//input[contains(@id, '" + id + "')]");
         return this.pulsaEnElUnico(objetoBuscado);
      }
      catch (Exception e) {
         this.warning(this.mensajeDeError(e));
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
      this.trace("pulsaEnElUnico->" + objetoBuscado);
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
         this.warning(this.mensajeDeError(e));
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
      this.debug("obtieneNumeroBotonesVisiblesEnListadoPara->" + titulo + "-" + clase);
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
         this.error(e);
         this.error(this.mensajeDeError(e));
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
      this.debug("pulsaElementoIesimoPara->" + pos + "-" + titulo + "-" + clase);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento siguiente
         conseguido = this.pulsaElementoIesimoParaAux(pos, titulo, clase);
      }
      if (!conseguido) {
         String mensaje = "No ha sido posible cliquear en elemento del listado de la posicion: " + pos;
         this.error(mensaje);
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
      this.trace("pulsaElementoIesimoParaAux->" + pos + "-" + titulo + "-" + clase);
      boolean pulsado = false;
      try {
         By objetoBuscado = By.xpath("//input[contains(@class, '" + clase + "') and contains(@title, '" + titulo + "')]");

         this.esperaCorta();

         // List<WebElement> webElements = WebUiCommonHelper.findWebElements(genericObject, GlobalVariable.tiempoRetrasoCorto)
         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);

         if (null != webElements && webElements.size() > 0 && webElements.size() >= pos) {
            this.trace("" + webElements.size());
            // Pulsamos el i-esimo
            this.resaltaObjeto(webElements.get(pos - 1), COLOR_AMARILLO);
            webElements.get(pos - 1).click();
            this.esperaCorta();

            pulsado = true;
         }
      }
      catch (Exception e) {
         this.warning(this.mensajeDeError(e));
      }
      return pulsado;
   }

   public void pulsaPrimerElementoDocsAnexos(String textoEnlace) throws PruebaAceptacionExcepcion {
      this.debug("pulsaPrimerElementoDocsAnexos->" + textoEnlace);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaPrimerElementoDocsAnexosAux(textoEnlace);
      }
      if (!conseguido) {
         String mensaje = "No existe un primer tipo de documentos anexos a seleccionar";
         this.error(mensaje);
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
      this.trace("pulsaPrimerElementoDocsAnexosAux->" + textoEnlace);

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
         this.warning(this.mensajeDeError(e));
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
      this.debug("clickParaUploadFicheroIesimoListado->" + rutaFichero + "-" + posicion);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.clickParaUploadFicheroIesimoListadoAux(rutaFichero, posicion);
      }
      if (!conseguido) {
         String mensaje = "No se puede hacer click en elemento i-esimo del listado: posicion = " + posicion;
         this.error(mensaje);
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
      this.trace("clickParaUploadFicheroIesimoListadoAux->" + rutaFichero + "-" + posicion);
      try {
         String id = ":" + (posicion - 1) + ":subirFichero:file";
         By objetoBuscado = By.xpath("//input[@type = 'file' and contains(@id, '" + id + "')]");

         WebElement elemento = this.esperaParaSubirFichero(objetoBuscado);
         elemento.sendKeys(rutaFichero);
         return true;
      }
      catch (Exception e) {
         this.warning("No se puede hacer clic en el fichero en posición: " + posicion + ". " + this.mensajeDeError(e));
      }
      return false;
   }

   /**
    * Pulsa sobre el unico elemento descargable en el listado de documentacion anexa.
    *
    * @throws PruebaAceptacionExcepcion
    */
   public void pulsaUnicoElementoDescargable() throws PruebaAceptacionExcepcion {
      this.debug("pulsaUnicoElementoDescargable");
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoDescargableAux();
      }
      if (!conseguido) {
         String mensaje = "No existe un unico elemento descargable en el listado";
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Intenta pulsar sobre el unico elemento descargable.
    *
    * @return Si consigue pulsar sobre el unico elemento descargable, devuelve 1; caso contrario, devuelve 0.
    */
   private boolean pulsaUnicoElementoDescargableAux() {
      this.trace("pulsaUnicoElementoDescargableAux");
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
         this.warning("No se puede hacer clic en el único elemento descargable: " + this.mensajeDeError(e));
      }
      return false;
   }

   /**
    * Pulsa sobre el único elemento a eliminar en el listado de documentación anexa.
    *
    * @throws PruebaAceptacionExcepcion
    */
   public void pulsaUnicoElementoEliminable() throws PruebaAceptacionExcepcion {
      this.debug("pulsaUnicoElementoEliminable");
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoElementoEliminableAux();
      }
      if (!conseguido) {
         String mensaje = "No se ha podido pulsar en el único elemento eliminable";
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Intenta pulsar sobre el unico elemento a eliminar en el listado de documentacion anexa.
    *
    * @return Si consigue pulsar sobre el unico elemento a eliminar, devuelve 1; caso contrario, devuelve 0.
    */
   private boolean pulsaUnicoElementoEliminableAux() {
      this.trace("pulsaUnicoElementoEliminableAux");
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
         this.warning("No se puede hacer clic en el único elemento eliminable: " + this.mensajeDeError(e));
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
      this.debug("clickCambiandoFoco->" + testObject.toString());
      try {
         this.click(testObject);
      }
      catch (PruebaAceptacionExcepcion e) {
         this.error(e);
         this.error(this.mensajeDeError(e));
         throw e;
      }
   }

   public WebElement clickConUrlAfirmaProtocol(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("clickConUrlAfirmaProtocol->" + testObject.toString());
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
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            this.ejecutaAccionesUrlAfirmaProtocol();
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "No se puede hacer click tras esperar el URL Afirma Protocol";
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return elemento;
   }

   private void ejecutaAccionesUrlAfirmaProtocol() throws PruebaAceptacionExcepcion {
      try {
         Robot rb = new Robot();

         this.esperaMedia();

         this.debug("Pulsar <DERECHA>");
         rb.keyPress(KeyEvent.VK_RIGHT);
         rb.keyRelease(KeyEvent.VK_RIGHT);
         this.debug("Soltar <DERECHA>");
         this.debug("Pulsar <INTRO>");
         rb.keyPress(KeyEvent.VK_ENTER);
         rb.keyRelease(KeyEvent.VK_ENTER);
         this.debug("Soltar <INTRO>");
      }
      catch (AWTException e) {
         String mensaje = "Error al manejar el robot";
         this.error(e);
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   // private void cambiarFoco() throws PruebaAceptacionExcepcion {
   // try {
   // Robot rb = new Robot();
   // debug("Cambiar el foco a Autofirma");
   // rb.keyPress(KeyEvent.VK_ALT);
   // rb.keyPress(KeyEvent.VK_TAB);
   // rb.delay(10);
   // rb.keyRelease(KeyEvent.VK_ALT);
   // rb.keyRelease(KeyEvent.VK_TAB);
   // rb.delay(1000);
   // }
   // catch (AWTException e) {
   // String mensaje = "Error al manejar el robot";
   // error(mensaje);
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
      this.debug("pulsaPrimerElementoPara->" + titulo + "-" + clase);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaPrimerElementoParaAux(titulo, clase);
      }
      if (!conseguido) {
         String mensaje = "No ha sido posible cliquear en el primer elemento del listado";
         this.error(mensaje);
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
      this.trace("pulsaPrimerElementoParaAux->" + titulo + "-" + clase);
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
         this.warning(this.mensajeDeError(e));
      }
      return false;
   }

   /**
    * Hace click sobre el objeto que contenga el id pasado por parámetro. Para ello, crea un objeto genérico con el id como propiedad
    */
   public void clickGenerico(String id) throws PruebaAceptacionExcepcion {
      this.debug("clickGenerico->" + id);
      TestObject genericObject = new TestObject();
      genericObject.addProperty("id", ConditionType.CONTAINS, id);
      this.click(this.convertirXpath(genericObject));
   }

   public void clickMensajePorTexto(String texto) throws PruebaAceptacionExcepcion {
      this.debug("clickMensajePorTexto->" + texto);
      TestObject genericObject = new TestObject();
      genericObject.addProperty("tag", ConditionType.EQUALS, "p");
      genericObject.addProperty("text", ConditionType.CONTAINS, texto);
      this.click(this.convertirXpath(genericObject));
   }

   public void clickNodoArbol(String id) throws PruebaAceptacionExcepcion {
      this.debug("clickNodoArbol->" + id);
      TestObject genericObject = new TestObject();
      genericObject.addProperty("id", ConditionType.CONTAINS, id);
      genericObject.addProperty("class", ConditionType.CONTAINS, "icon-collapsed");
      this.click(this.convertirXpath(genericObject));
   }

   public By convertirXpath(TestObject genericObject) {
      this.trace("convertirXpath->" + genericObject);
      XPathBuilder xpath = new XPathBuilder(genericObject.getProperties());
      return By.xpath(xpath.build());
   }

   public boolean isElementoClickable(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("isElementoClickable->" + testObject);
      WebElement elemento = null;
      try {
         WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
               Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_CORTO))),
               Duration.ofMillis(100));
         elemento = wait.until(ExpectedConditions.elementToBeClickable(testObject));
      }
      catch (TimeoutException | StaleElementReferenceException e) {
         String mensaje = "El objeto " + testObject.toString() + " no es clickable";
         this.warning(mensaje);
      }
      catch (PruebaAceptacionExcepcion e) {
         this.error(e);
         this.error(this.mensajeDeError(e));
         throw e;
      }
      return elemento != null;
   }

   /**
    * Pulsa sobre el unico boton que debe existir en la pagina de detalle/formulario en la parte de las acciones a realizar que aparecen en
    * la parte inferior de la pagina que contenga en su titulo el parametro {@code titulo} y en su class el parametro {@code clase}. Ademas
    * ese elemento debe ser un 'tag = input' y de 'type = button' o 'type = submit'. NOTA - esto es para pulsar sobre los botones de accion
    * que aparecen en la parte inferior de la pagina de detalle/formulario, y tener en un lugar centralizado lo que se debe hacer con lo que
    * si cambia algo, se cambia el codigo de este metodo de forma unificada, aunque dejen de tener sentido los nombres de los parametros.
    *
    * @param genericObjectPath
    *           path hacia el objeto generico a utilizar.
    * @param titulo
    *           a considerar para el title.
    * @param clase
    *           a considerar para la clase CSS.
    * @throws PruebaAceptacionExcepcion
    */
   public void pulsaUnicoBotonAccionesInferioresPara(String titulo, String clase) throws PruebaAceptacionExcepcion {
      this.debug("pulsaUnicoBotonAccionesInferioresPara->" + titulo + "-" + clase);
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaUnicoBotonAccionesInferioresParaAux(titulo, clase, "button")
               || this.pulsaUnicoBotonAccionesInferioresParaAux(titulo, clase, "submit");
      }
      if (!conseguido) {
         String mensaje = "No existe un unico boton a cliquear en la parte inferior de acciones";
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Pulsa sobre el unico boton que debe existir en la pagina de detalle/formulario en la parte de las acciones a realizar que aparecen en
    * la parte inferior de la pagina que contenga en su titulo el parametro {@code titulo}, en su class el parametro {@code clase}, y en su
    * type el parametro {@code tipo}. Ademas ese elemento debe ser un 'tag = input'. NOTA - esto es para pulsar sobre los botones de accion
    * que aparecen en la parte inferior de la pagina de detalle/formulario, y tener en un lugar centralizado lo que se debe hacer con lo que
    * si cambia algo, se cambia el codigo de este metodo de forma unificada, aunque dejen de tener sentido los nombres de los parametros.
    *
    * @param titulo
    *           a considerar para el title.
    * @param clase
    *           a considerar para la clase CSS.
    * @param tipo
    *           a considerar para type.
    * @return devuelve true si consigue pulsar sobre ese unico boton; en caso contrario, devuelve false.
    */
   private boolean pulsaUnicoBotonAccionesInferioresParaAux(String titulo, String clase, String tipo) {
      TestObject genericObject = new TestObject();
      genericObject.addProperty("class", ConditionType.CONTAINS, clase);
      genericObject.addProperty("title", ConditionType.CONTAINS, titulo);
      genericObject.addProperty("tag", ConditionType.EQUALS, "input");
      genericObject.addProperty("type", ConditionType.EQUALS, tipo);
      return this.pulsaEnElUnico(this.convertirXpath(genericObject));
   }

}