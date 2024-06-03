package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory;
import lombok.extern.slf4j.Slf4j;


/**
 * Clase que contiene las funcionalidades de selenium encapsulada, para usarlo de manera mas sencilla y con metodos
 * específicos para los componentes de RichFace
 *
 * @author AGAPA
 */
@Slf4j
public class WebElementWrapperPrimeFace extends WebElementWrapper {

   public void mostrarElementoPresente(By testObject) throws PruebaAceptacionExcepcion {
      WebElementWrapperPrimeFace.log.debug("mostrarElementoPresente->" + testObject.toString());

      boolean conseguido = false;
      WebElement elemento = null;
      for (int i = 1; !conseguido && i <= WebElementWrapper.NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elemento = this.esperarHastaQueElementoPresente(testObject);
            try {
               JavascriptExecutor js = (JavascriptExecutor) WebDriverFactory.getDriver();
               js.executeScript("arguments[0].setAttribute('style', arguments[1]);", elemento, "display: block;");
            }
            catch (Exception e) {
               String mensaje = "El elemento " + elemento + " no puede ser mostrado";
               WebElementWrapperPrimeFace.log.error(mensaje);
               throw new PruebaAceptacionExcepcion(mensaje);
            }
            conseguido = true;
         }
         catch (PruebaAceptacionExcepcion e) {
            conseguido = false;
         }
      }
      if (!conseguido) {
         String mensaje = "El elemento " + testObject.toString() + " no puede ser mostrado";
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }

   }

   public void seleccionaDesplegableByLabel(String idDivSelect, String labelBuscada) throws PruebaAceptacionExcepcion {
      WebElementWrapperPrimeFace.log.debug("seleccionaDesplegableByLabel->" + idDivSelect + ", " + labelBuscada);
      By label = By.id(idDivSelect + "_label");
      By filtro = By.id(idDivSelect + "_filter");
      this.seleccionaDesplegableByLabel(labelBuscada, label, filtro);
   }

   public void seleccionaDesplegableByLabel(String labelBuscada, By label, By filtro) throws PruebaAceptacionExcepcion {
      WebElementWrapperPrimeFace.log.debug("seleccionaDesplegableByLabel->" + label.toString());
      boolean conseguido = false;
      WebElement elementoLabel = null;
      for (int i = 1; !conseguido && i <= WebElementWrapper.NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elementoLabel = this.esperaCompleta(label);
            this.resaltaObjeto(elementoLabel, WebElementWrapper.COLOR_AMARILLO);
            this.esperarHastaQueElementoClickable(elementoLabel).click();
            this.esperarHastaQueElementoVisible(filtro).click();
            this.escribeTexto(filtro, labelBuscada);
            conseguido = true;
         }
         catch (Exception e) {
            conseguido = false;
         }
      }
      if (!conseguido) {
         String mensaje = "Error al seleccionar " + labelBuscada + " del elemento " + label.toString();
         this.error(mensaje);
         throw new PruebaAceptacionExcepcion(mensaje);
      }
   }

   public int obtenerNumeroRegistrosTabla(By tablaResultado) {
      // TODO Auto-generated method stub
      return 0;
   }

   public void selectOneMenu(String string, String string2) {
      // TODO Auto-generated method stub

   }

   public void esperarDesaparezcaElemento(By barraProgreso) {
      // TODO Auto-generated method stub

   }

   public String obtenerTextoElemento(By resSwCsv) {
      // TODO Auto-generated method stub
      return null;
   }

   public void setEsperarMilisegundosEsperaObligatoria(boolean b) {
      // TODO Auto-generated method stub

   }

}
