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
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * Clase que contiene las funcionalidades de selenium encapsulada, para usarlo de manera mas sencilla
 *
 * @author AGAPA
 */
@Slf4j
public class WebElementWrapper {

   private final static String COLOR_AMARILLO         = "yellow"; // Interacción

   private final static String COLOR_AZUL             = "cyan";   // Comprobación

   private final static int    NUMERO_MAXIMO_INTENTOS = 5;

   private final static int    NUMERO_MINIMO_INTENTOS = 2;

   private String              idElementoProcesando   = null;

   public void debug(String mensaje) {
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
      if (!mensaje.startsWith("stale element reference: stale element not found")) {
         WebDriverFactory.getLogger().warning(mensaje);
      }
   }

   public void error(String mensaje) {
      log.error(mensaje);
      WebDriverFactory.getLogger().fail(mensaje);
   }

   public void error(Throwable t) {
      log.error(t.getLocalizedMessage());
      WebDriverFactory.getLogger().log(Status.FAIL, t);
   }

   public String mensajeDeError(Exception e) {
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
            // FIXME: Modificar para que pueda esperarHastaQueElementoPadreContengaHijo
            // By opcion = By.xpath("(//option)[" + index + "]");
            // this.esperarHastaQueElementoPadreContengaHijo(elemento, opcion);
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

   public void selectOptionByIndex(By testObject, Integer index, boolean conRetraso) throws PruebaAceptacionExcepcion {
      this.debug("selectOptionByIndex->" + testObject.toString() + ". Index=" + index + ". Con retraso=" + conRetraso);
      try {
         this.selectOptionByIndex(testObject, index);
         if (conRetraso) {
            this.esperaCorta();
         }
      }
      catch (PruebaAceptacionExcepcion e) {
         String mensaje = "Error en " + testObject.toString()
               + " al hacer seleccionar el valor del combo por index con retraso. Motivo del error: " + this.mensajeDeError(e);
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
            By opcion = By.xpath("//option[(text() = '" + label + "' or . = '" + label + "')]");
            this.esperaElementoConCondicionMedia(ExpectedConditions.presenceOfNestedElementLocatedBy(elemento, opcion));
            // this.esperarHastaQueElementoPadreContengaHijo(elemento, opcion);
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

   public void verifyElementText(By testObject, String texto) throws PruebaAceptacionExcepcion {
      this.debug("verifyElementText->" + testObject.toString() + ". Text=" + texto);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaCompleta(testObject);
            if (elemento == null) {
               throw new PruebaAceptacionExcepcion(testObject.toString() + " no existe");
            }
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = this.esperaConCondicionMedia(ExpectedConditions.textToBe(testObject, texto));
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error en " + testObject.toString() + " al verificar el texto del elemento " + testObject.toString()
               + ". Texto esperado: " + texto;
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public void verifyElementPresent(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("verifyElementPresent->" + testObject.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         if (this.isElementoPresente(testObject)) {
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
            this.trace("Valor del atributo=" + valorAtributo);
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
         this.warning(this.mensajeDeError(e));
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
         String mensaje = "Error al obtener el texto del elemento " + testObject.toString();
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
   // FIXME: Hacerlo genérico
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

            WebElement table = this.esperaCompleta(By.id(idBodyTabla));
            List<WebElement> rows = table.findElements(By.xpath("//table[@id='" + idBodyTabla + "']/tbody/tr"));
            numeroDeFilas = rows.size();
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al obtener el número de fila de la tabla con ID=" + idBodyTabla;
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return numeroDeFilas;
   }

   public void esperaIncondicional(int segundos) {
      this.debug("esperaIncondicional-> " + segundos + " segundos");
      try {
         Thread.sleep(segundos * 1000);
      }
      catch (InterruptedException e) {
         // Seguramente nunca se produzca esta excepción
         this.error("Error en espera incondicional");
      }
   }

   /**
    * Posicionar en porsición más alta de la página
    */
   public void scrollTopPagina() { // FIXME: Borrar si no se utiliza
      this.debug("scrollTopPagina");
      ((JavascriptExecutor) WebDriverFactory.getDriver()).executeScript("window.scrollTo(0, -document.body.scrollHeight)");
   }

   private boolean esperarHastaQueElementoNoPresente(By testObject) throws WebDriverException {
      this.trace("esperarHastaQueElementoNoPresente->" + testObject.toString());
      boolean conseguido = false;
      try {
         conseguido = this.esperaConCondicionLarga(ExpectedConditions.not(ExpectedConditions.presenceOfAllElementsLocatedBy(testObject)));
      }
      catch (WebDriverException e) {
         String mensaje = this.mensajeDeError(e);
         this.warning(mensaje);
         throw e;
      }
      return conseguido;
   }

   private boolean esperaConCondicionLarga(ExpectedCondition<Boolean> condicion) throws WebDriverException {
      return this.esperaConCondicion(condicion, PropiedadesTest.TIEMPO_RETRASO_LARGO);
   }

   private boolean esperaConCondicionMedia(ExpectedCondition<Boolean> condicion) throws WebDriverException {
      return this.esperaConCondicion(condicion, PropiedadesTest.TIEMPO_RETRASO_MEDIO);
   }

   private WebElement esperaElementoConCondicionMedia(ExpectedCondition<WebElement> condicion) throws WebDriverException {
      return this.esperaElementoConCondicion(condicion, PropiedadesTest.TIEMPO_RETRASO_MEDIO);
   }

   private WebElement esperaElementoConCondicionCorta(ExpectedCondition<WebElement> condicion) throws WebDriverException {
      return this.esperaElementoConCondicion(condicion, PropiedadesTest.TIEMPO_RETRASO_CORTO);
   }

   private boolean esperaConCondicion(ExpectedCondition<Boolean> condicion, PropiedadesTest tiempo) throws WebDriverException {
      this.trace("esperaConCondicion->condición=" + condicion.toString() + ", tiempo=" + tiempo);
      boolean conseguido = false;
      WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(tiempo))), Duration.ofMillis(100));
      try {
         conseguido = wait.until(condicion);
      }
      catch (WebDriverException e) {
         String mensaje = this.mensajeDeError(e);
         this.warning(mensaje);
         throw e;
      }
      return conseguido;
   }

   private WebElement esperaElementoConCondicion(ExpectedCondition<WebElement> condicion, PropiedadesTest tiempo)
         throws WebDriverException {
      WebElement elemento;
      WebDriverWait wait = new WebDriverWait(WebDriverFactory.getDriver(),
            Duration.ofSeconds(Integer.parseInt(VariablesGlobalesTest.getPropiedad(tiempo))), Duration.ofMillis(100));
      try {
         elemento = wait.until(condicion);
      }
      catch (WebDriverException e) {
         String mensaje = this.mensajeDeError(e);
         this.warning(mensaje);
         throw e;
      }
      return elemento;
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

   /**
    * Devuelve si un elemento está presente, con una espera breve
    *
    * @param testObject
    *           valor para: test object
    * @return true, si se cumple que el valor del atributo: objeto presente es cierto.
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public boolean isElementoPresente(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("isElementoPresente->" + testObject.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MINIMO_INTENTOS; i++) {
         try {
            WebElement elemento = this.esperaBreve(testObject);
            this.resaltaObjeto(elemento, COLOR_AZUL);
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
         }
      }
      if (!conseguido) {
         String mensaje = "El objeto " + testObject.toString() + " NO está presente";
         this.debug(mensaje);
      }
      return conseguido;
   }

   /**
    * Devuelve si un elemento está presente, sin espera ninguna
    *
    * @param testObject
    *           valor para: test object
    * @return true, si se cumple que el valor del atributo: objeto presente es cierto.
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public boolean existeElemento(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("existeElemento->" + testObject.toString());
      boolean existe = false;
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            existe = WebDriverFactory.getDriver().findElements(testObject).size() > 0;
            conseguido = true;
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al comprobar si existe " + testObject.toString();
         if (excepcion != null) {
            mensaje += ". Motivo del error: " + this.mensajeDeError(excepcion);
            this.error(excepcion);
         }
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return existe;
   }

   private void esperarDesaparezcaProcesando() throws PruebaAceptacionExcepcion {
      this.trace("esperarDesaparezcaProcesando");
      this.obtenerIdElementoProcesando();
      if (this.idElementoProcesando != null) {
         By by = By.id(this.idElementoProcesando);
         this.trace("Buscar " + this.idElementoProcesando);
         List<WebElement> elementos = WebDriverFactory.getDriver().findElements(by);
         this.trace("Fin de la búsqueda");
         try {
            if (elementos.size() > 0 && elementos.get(0).isDisplayed()) {
               this.trace(this.idElementoProcesando + " encontrados: " + elementos.size());
               int tiempo = 0;
               while (elementos.get(0).isDisplayed()) {
                  this.trace(this.idElementoProcesando + " visible");
                  try {
                     this.trace("Espera 100 ms");
                     Thread.sleep(100);
                  }
                  catch (InterruptedException e) {
                     // Seguramente nunca se produzca esta excepción
                     this.warning("Error al parar el procesamiento del hilo de ejecución");
                  }
                  tiempo += 100;
                  if (tiempo > Integer.parseInt(VariablesGlobalesTest.getPropiedad(PropiedadesTest.TIEMPO_RETRASO_MEDIO)) * 1000) {
                     String mensaje = "La ventana \"Procesando...\" no desaparece";
                     this.warning(mensaje);
                     throw new PruebaAceptacionExcepcion(mensaje);
                  }
               }
            }
            else {
               this.trace("Procesando no estaba");
            }
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            this.trace("La ventana procesando ya no está visible");
         }
      }
   }

   private void obtenerIdElementoProcesando() {
      try {
         this.idElementoProcesando = VariablesGlobalesTest.getPropiedad(PropiedadesTest.ID_ELEMENTO_PROCESANDO);
      }
      catch (IllegalArgumentException e) {
         this.warning("ID_ELEMENTO_PROCESANDO no definido en fichero properties");
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
      try {
         elemento = this.esperaElementoConCondicionMedia(ExpectedConditions.visibilityOfElementLocated(testObject));
      }
      catch (WebDriverException e) {
         String mensaje = "Error al esperar brevemente que el objeto " + testObject.toString() + " sea visible";
         this.warning(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
      return elemento;
   }

   private WebElement esperarHastaQueElementoVisible(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoVisible->" + testObject.toString());
      WebElement elemento = null;
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaElementoConCondicionMedia(ExpectedConditions.visibilityOfElementLocated(testObject));
            conseguido = true;
         }
         catch (WebDriverException e) {
            String mensaje = "Error al esperar que el objeto " + testObject.toString() + " sea visible";
            this.warning(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return elemento;
   }

   private void esperarHastaQueElementoPresenteBreve(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoPresenteBreve->" + testObject.toString());
      try {
         this.esperaElementoConCondicionCorta(ExpectedConditions.presenceOfElementLocated(testObject));
      }
      catch (WebDriverException e) {
         String mensaje = "Error al esperar brevemente que el objeto " + testObject.toString() + " esté presente";
         this.warning(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   private WebElement esperarHastaQueElementoPresente(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoPresente->" + testObject.toString());
      WebElement elemento = null;
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaElementoConCondicionMedia(ExpectedConditions.presenceOfElementLocated(testObject));
            conseguido = true;
         }
         catch (WebDriverException e) {
            String mensaje = "Error al esperar que el objeto " + testObject.toString() + " esté presente";
            this.warning(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return elemento;
   }

   private WebElement esperarHastaQueElementoClickable(WebElement testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoClickable->" + testObject.getAttribute("id"));
      WebElement elemento = null;
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaElementoConCondicionMedia(ExpectedConditions.elementToBeClickable(testObject));
            conseguido = true;
         }
         catch (WebDriverException e) {
            String mensaje = "Error al esperar que el objeto " + testObject.toString() + " sea clickable";
            this.warning(mensaje);
            throw new PruebaAceptacionExcepcion(mensaje);
         }
      }
      return elemento;
   }

   public void esperarHastaQueElementoClickable(By testObject) throws PruebaAceptacionExcepcion {
      this.trace("esperarHastaQueElementoClickable->" + testObject);
      Exception excepcion = null;
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            this.esperaElementoConCondicionMedia(ExpectedConditions.elementToBeClickable(testObject));
            conseguido = true;
         }
         catch (WebDriverException e) {
            this.warning(this.mensajeDeError(e));
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al esperar que el objeto " + testObject.toString() + " sea clickable";
         this.error(excepcion);
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
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
      this.esperaConcreta(PropiedadesTest.TIEMPO_RETRASO_CORTO);
   }

   private void esperaMedia() {
      this.esperaConcreta(PropiedadesTest.TIEMPO_RETRASO_MEDIO);
   }

   public void retrasoLargo() {
      this.esperaConcreta(PropiedadesTest.TIEMPO_RETRASO_LARGO);
   }

   private void esperaAdicionalAutofirma() {
      this.esperaConcreta(PropiedadesTest.TIEMPO_RETRASO_AUTOFIRMA);
   }

   private void esperaConcreta(PropiedadesTest tiempo) {
      try {
         this.esperarDesaparezcaProcesando();
         Thread.sleep(Integer.parseInt(VariablesGlobalesTest.getPropiedad(tiempo)) * 1000);
      }
      catch (Exception e) {
         this.warning(this.mensajeDeError(e));
      }
   }

   /**
    * Pulsa sobre el unico elemento que debe existir.
    */
   public void pulsaUnicoElemento(By objetoBuscado) throws PruebaAceptacionExcepcion {
      this.debug("pulsaUnicoElemento->" + objetoBuscado.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento i-esimo
         conseguido = this.pulsaEnElUnico(objetoBuscado);
      }
      if (!conseguido) {
         String mensaje = "No se ha podido pulsar el único objeto " + objetoBuscado.toString();
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Busca el objeto {@code objetoBuscado} y si solo existe él y le hace clic.
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
    * Devuelve el numero de elementos de una pagina que cumplen las condiciones indicadas.
    *
    * @return el numero de botones visibles en el listado de una pagina que sea un listado, que verifican las condiciones indicadas.
    */
   public int obtieneNumeroDeElementos(By objetosBuscados) throws PruebaAceptacionExcepcion {
      this.debug("obtieneNumeroDeElementos->" + objetosBuscados.toString());
      try {
         int num = 0;

         this.esperaCorta();

         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetosBuscados);

         if (null != webElements) {
            num = webElements.size();
         }
         return num;
      }
      catch (Exception e) {
         this.error(e);
         this.error(this.mensajeDeError(e));
         throw new PruebaAceptacionExcepcion("No ha sido posible obtener el número de elementos que cumplan " + objetosBuscados.toString());
      }
   }

   /**
    * Se intenta pulsar sobre el elemento i-esimo del listado. Si no se consigue se deja constancia de ello.
    */
   public void pulsaElementoIesimo(int pos, By objetoBuscado) throws PruebaAceptacionExcepcion {
      this.debug("pulsaElementoIesimoPara->" + objetoBuscado.toString());
      boolean conseguido = false;
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         // Intento siguiente
         conseguido = this.pulsaElementoIesimoAux(pos, objetoBuscado);
      }
      if (!conseguido) {
         String mensaje = "No ha sido posible cliquear en elemento del listado de la posicion: " + pos;
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   /**
    * Se pulsa en el elemento i-esimo de acuerdo a las condiciones indicadas.
    *
    * @return si se consigue pulsar sobre este elemento i-esimo o no.
    */
   private boolean pulsaElementoIesimoAux(int pos, By objetoBuscado) {
      this.trace("pulsaElementoIesimoParaAux->" + pos + "-" + objetoBuscado.toString());
      boolean pulsado = false;
      try {
         // this.esperaCorta();
         this.esperaCompleta(objetoBuscado);

         List<WebElement> webElements = WebDriverFactory.getDriver().findElements(objetoBuscado);

         if (null != webElements && webElements.size() > 0 && webElements.size() >= pos) {
            this.trace("Nº de elementos encontrados: " + webElements.size());
            // Pulsamos el i-esimo
            this.resaltaObjeto(webElements.get(pos - 1), COLOR_AMARILLO);
            webElements.get(pos - 1).click();
            // this.esperaCorta();

            pulsado = true;
         }
      }
      catch (Exception e) {
         this.warning(this.mensajeDeError(e));
      }
      return pulsado;
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

   public WebElement seleccionarCertificadoAutofirma(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("seleccionarCertificadoAutofirma->" + testObject.toString());
      this.obtenerIdElementoProcesando();
      boolean conseguido = false;
      WebElement elemento = null;
      Exception excepcion = null;
      this.ejecutaAccionesUrlAfirmaProtocol();
      for (int i = 1; !conseguido && i <= NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperaCompleta(testObject);
            this.resaltaObjeto(elemento, COLOR_AMARILLO);
            // WebElement elementoClickable = this.esperarHastaQueElementoClickable(elemento);
            // if (elementoClickable != null) {
            // elementoClickable.click();
            conseguido = true;
            // }
         }
         catch (Exception e) {
            this.warning(this.mensajeDeError(e));
            boolean existeProcesando = false;
            if (this.idElementoProcesando != null) {
               By by = By.id(this.idElementoProcesando);
               this.trace("Buscar " + this.idElementoProcesando);
               List<WebElement> elementos = WebDriverFactory.getDriver().findElements(by);
               this.trace("Fin de la búsqueda");
               if (elementos.size() > 0 && elementos.get(0).isDisplayed()) {
                  this.trace(this.idElementoProcesando + " encontrados: " + elementos.size());
                  existeProcesando = true;
                  // Si ya ha aparecido la ventana "Procesando..." es que ya se ha seleccionado el certificado en Autofirma
               }
               else {
                  this.trace("Procesando no estaba");
               }
            }
            if (!existeProcesando) {
               this.ejecutaAccionesUrlAfirmaProtocol();
            }
            excepcion = e;
         }
      }
      if (!conseguido) {
         String mensaje = "No se puede seleccionar el certificado con Autofirma";
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

         log.info("Espera media...");
         this.esperaMedia();
         log.info("Espera adicional...");
         this.esperaAdicionalAutofirma();

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

   public By convertirXpath(TestObject genericObject) {
      this.trace("convertirXpath->" + genericObject);
      XPathBuilder xpath = new XPathBuilder(genericObject.getProperties());
      return By.xpath(xpath.build());
   }

   public boolean isElementoClickable(By testObject) throws PruebaAceptacionExcepcion {
      this.debug("isElementoClickable->" + testObject);
      WebElement elemento = null;
      try {
         elemento = this.esperaElementoConCondicionCorta(ExpectedConditions.elementToBeClickable(testObject));
      }
      catch (WebDriverException e) {
         String mensaje = "El objeto " + testObject.toString() + " no es clickable";
         this.warning(mensaje);
      }
      catch (Exception e) {
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