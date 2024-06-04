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

   public int obtenerNumeroRegistrosTabla(By tabla) throws PruebaAceptacionExcepcion {
      WebElementWrapperPrimeFace.log.debug("obtenerNumeroRegistrosTabla->" + tabla.toString());
      By spanPaginator = By.cssSelector("span.ui-paginator-current");
      return this.obtenerNumeroRegistrosTabla(tabla, spanPaginator, 1);
   }

   /**
    * Devuelve el número de elementos de la tabla tomándolo de currentPageReportTemplate
    *
    * @param tabla
    *           valor para: tabla
    * @param spanPaginator
    *           valor para: elemento que muestra el texto de currentPageReportTemplate
    * @param position
    *           valor para: posición del número de elementos en el currentPageReportTemplate. Ej: Total {totalRecords}
    *           elementos - Página {currentPage} de {totalPages}. En este caso el totalRecords está en la posición 1
    *           (empieza por el 0).
    * @return int Número de elementos en la tabla
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public int obtenerNumeroRegistrosTabla(By tabla, By spanPaginator, int position) throws PruebaAceptacionExcepcion {
      WebElementWrapperPrimeFace.log.debug("obtenerNumeroRegistrosTabla->" + tabla.toString());
      WebElement t = this.esperaCompleta(tabla);
      WebElement paginador = t.findElement(spanPaginator);
      String cadena = paginador.getText();
      String[] aux = cadena.split(" ");
      String res = aux[position];
      return Integer.valueOf(res);
   }

   public void selectOneMenu(String id, String label) throws PruebaAceptacionExcepcion {
      By selectOneMenu = By.id(id + "_label");
      By opcion = By.xpath("//*[@id='" + id + "_panel']/div/ul/li[text()='" + label + "']");
      WebElement weCombo = this.click(selectOneMenu);
      WebElement weValue = this.click(opcion);

      // weCombo.sendKeys(Keys.TAB.toString()); // Hay veces que si no se pulsa TAB, no funciona

      this.esperaIncondicionalMilisegundos(300);
      WebElementWrapperPrimeFace.log.info("Ver si está abierto...");
      if (weValue.isDisplayed()) {
         WebElementWrapperPrimeFace.log.info("...está 'displayed'... lo cierro...");
         this.click(selectOneMenu); // Para cerrar y que no tape nada.
      }
      WebElementWrapperPrimeFace.log.info("... fin de selectOneMenu");

   }

}
