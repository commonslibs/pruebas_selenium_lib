package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest.PropiedadesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;


/**
 * Clase que contiene las funcionalidades de selenium encapsulada, para usarlo de manera mas sencilla y con metodos específicos para los
 * componentes de RichFace.
 *
 * @author AGAPA
 */
@Slf4j
public class WebElementWrapperPrimeFace extends WebElementWrapper {

   /** milisegundos espera obligatoria. */
   private long    milisegundosEsperaObligatoria        = 0;

   /** esperar milisegundos espera obligatoria. */
   private boolean esperarMilisegundosEsperaObligatoria = false;

   /** variables cargadas. */
   private boolean variablesCargadas                    = false;

   /**
    * Comprueba el valor del atributo: esperar milisegundos espera obligatoria.
    *
    * @return true, si se cumple que el valor del atributo: esperar milisegundos espera obligatoria es cierto.
    */
   public boolean isEsperarMilisegundosEsperaObligatoria() {
      if (!this.variablesCargadas) {
         this.cargarVariables();
      }
      return this.esperarMilisegundosEsperaObligatoria;
   }

   /**
    * Obtención del atributo: milisegundos espera obligatoria.
    *
    * @return atributo: milisegundos espera obligatoria
    */
   public long getMilisegundosEsperaObligatoria() {
      if (!this.variablesCargadas) {
         this.cargarVariables();
      }
      return this.milisegundosEsperaObligatoria;
   }

   /**
    * Cargar variables.
    */
   private void cargarVariables() {
      String valorEsperaObligatoria;
      try {
         valorEsperaObligatoria = VariablesGlobalesTest.getPropiedad(PropiedadesTest.MILISEGUNDOS_ESPERA_OBLIGATORIA).trim();
      }
      catch (Exception e) {
         WebElementWrapperPrimeFace.log.error("Se establece el valor de MILISEGUNDOS_ESPERA_OBLIGATORIA a cero.", e);
         valorEsperaObligatoria = "0";
      }
      this.milisegundosEsperaObligatoria = Long.parseLong(valorEsperaObligatoria);

      this.variablesCargadas = true;
   }

   /**
    * Mostrar elemento presente.
    *
    * @param testObject
    *           valor para: test object
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
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

   /**
    * Selecciona desplegable by label.
    *
    * @param idDivSelect
    *           valor para: id div select
    * @param labelBuscada
    *           valor para: label buscada
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public void seleccionaDesplegableByLabel(String idDivSelect, String labelBuscada) throws PruebaAceptacionExcepcion {
      WebElementWrapperPrimeFace.log.debug("seleccionaDesplegableByLabel->" + idDivSelect + ", " + labelBuscada);
      By label = By.id(idDivSelect + "_label");
      By filtro = By.id(idDivSelect + "_filter");
      this.seleccionaDesplegableByLabel(labelBuscada, label, filtro);
   }

   /**
    * Selecciona desplegable by label.
    *
    * @param labelBuscada
    *           valor para: label buscada
    * @param label
    *           valor para: label
    * @param filtro
    *           valor para: filtro
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
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

   /**
    * Obtener numero registros tabla.
    *
    * @param tabla
    *           valor para: tabla
    * @return int
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public int obtenerNumeroRegistrosTabla(By tabla) throws PruebaAceptacionExcepcion {
      WebElementWrapperPrimeFace.log.debug("obtenerNumeroRegistrosTabla->" + tabla.toString());
      By spanPaginator = By.cssSelector("span.ui-paginator-current");
      return this.obtenerNumeroRegistrosTabla(tabla, spanPaginator, 1);
   }

   /**
    * Devuelve el número de elementos de la tabla tomándolo de currentPageReportTemplate.
    *
    * @param tabla
    *           valor para: tabla
    * @param spanPaginator
    *           valor para: elemento que muestra el texto de currentPageReportTemplate
    * @param position
    *           valor para: posición del número de elementos en el currentPageReportTemplate. Ej: Total {totalRecords} elementos - Página
    *           {currentPage} de {totalPages}. En este caso el totalRecords está en la posición 1 (empieza por el 0).
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
      return Integer.parseInt(res);
   }

   /**
    * Select one menu.
    *
    * @param id
    *           valor para: id
    * @param label
    *           valor para: label
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public void selectOneMenu(String id, String label) throws PruebaAceptacionExcepcion {
      By selectOneMenu = By.id(id + "_label");
      By opcion = By.xpath("//*[@id='" + id + "_panel']/div/ul/li[text()='" + label + "']");
      this.click(selectOneMenu);
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

   /**
    * Modificación del atributo: esperar milisegundos espera obligatoria.
    *
    * @param esperar
    *           valor para: esperar
    */
   public void setEsperarMilisegundosEsperaObligatoria(boolean esperar) {
      this.esperarMilisegundosEsperaObligatoria = esperar;
   }

   /**
    * SOBREESCRITA - PARA OBLIGAR UNA ESPERA FORZOSA. ES GENERICA A CUALQUIER ACCION Y SE CONFIGURA EN EL .properties (Por Defecto irá
    * destactivada.)
    *
    * @param testObject
    *           valor para: test object
    * @return web element
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   @Override
   public WebElement esperaCompleta(By testObject) throws PruebaAceptacionExcepcion {
      this.esperarObligada();
      return super.esperaCompleta(testObject);
   }

   /**
    * Esperar obligada.
    */
   public void esperarObligada() {
      try {
         if (this.esperarMilisegundosEsperaObligatoria && this.milisegundosEsperaObligatoria > 0) {
            Thread.sleep(this.milisegundosEsperaObligatoria);
         }
      }
      catch (InterruptedException e) {
         WebElementWrapperPrimeFace.log.error("Error en la esperaObligada", e);
         Thread.currentThread().interrupt();
      }
   }

   @Override
   public int obtenerFilaConTextoEnTabla(By testObject, String texto) throws PruebaAceptacionExcepcion {
      return this.obtenerFilaConTextoEnTabla(testObject, texto, "table__row");
   }

}
