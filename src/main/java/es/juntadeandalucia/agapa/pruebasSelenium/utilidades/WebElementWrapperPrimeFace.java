package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactoryPrimeFace;
import org.openqa.selenium.*;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import es.juntadeandalucia.agapa.pruebasSelenium.utilidades.VariablesGlobalesTest.PropiedadesTest;
import es.juntadeandalucia.agapa.pruebasSelenium.webdriver.WebDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


/**
 * Clase que contiene las funcionalidades de selenium encapsulada, para usarlo de manera mas sencilla y con metodos
 * específicos para los componentes de RichFace.
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
         valorEsperaObligatoria =
               VariablesGlobalesTest.getPropiedad(PropiedadesTest.MILISEGUNDOS_ESPERA_OBLIGATORIA).trim();
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
      By panel = By.id(idDivSelect + "_panel");
      this.seleccionaDesplegableByLabel(labelBuscada, label, filtro, panel);
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
   public void seleccionaDesplegableByLabel(String labelBuscada, By label, By filtro, By panel)
         throws PruebaAceptacionExcepcion {
      WebElementWrapperPrimeFace.log.debug("seleccionaDesplegableByLabel->" + label.toString());
      boolean conseguido = false;
      WebElement elementoLabel = null;
      for (int i = 1; !conseguido && i <= WebElementWrapper.NUMERO_MAXIMO_INTENTOS; i++) {
         try {
            elementoLabel = this.esperaCompleta(label);
            this.resaltaObjeto(elementoLabel, WebElementWrapper.COLOR_AMARILLO);

            int intentos = 3;
            boolean panelDisplay = false;
            do {
               intentos--;
               try {
                  this.esperarHastaQueElementoClickable(elementoLabel).click();

                  this.esperaIncondicionalMilisegundos(100);

                  WebElement element = this.esperarHastaQueElementoVisible(panel);
                  String style = element.getAttribute("style");
                  panelDisplay = (style != null && style.contains("display: block"));
               }
               catch (Exception e) {
                  // Continuamos
                  this.trace("No se encuentra el panel desplegable " + panel.toString());
               }

            } while (!(panelDisplay || intentos <= 0));

            this.esperarHastaQueElementoVisible(filtro).click();
            this.esperaIncondicionalMilisegundos(100);
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
    * Comprueba si existe o no una opcion label en el desplegable, devolviendo TRUE de haberla podido seleccionar y
    * encontrado el texto y FALSE en caso contrario
    *
    * @param idDivSelect
    *           valor para: id div select
    * @param labelBuscada
    *           valor para: label buscada
    * @return boolean
    */
   public Boolean checkTextoInDesplegableByLabel(String idDivSelect, String labelBuscada) {
      try {
         WebElementWrapperPrimeFace.log.debug("checkTextoInDesplegableByLabel->" + idDivSelect + ", " + labelBuscada);
         By label = By.id(idDivSelect + "_label");
         By filtro = By.id(idDivSelect + "_filter");
         By panel = By.id(idDivSelect + "_panel");
         // Intentamos seleccionar la opcion que queremos checkear del desplegable, no deberia de seleccionar nada.
         this.seleccionaDesplegableByLabel(labelBuscada, label, filtro, panel);
         // Comprobamos si el labelBuscada está en el desplegable, porque se ha podido seleccionar
         this.verifyElementAttributeValue(label, "innerText", labelBuscada);
         WebElementWrapperPrimeFace.log.debug("Elemento {} está presente en el desplegable.", labelBuscada);
         return Boolean.TRUE;
      }
      catch (PruebaAceptacionExcepcion e) {
         // No se ha encontrado el elemento en el desplegable, porque no se ha seleccionado nada
         WebElementWrapperPrimeFace.log.debug("Elemento {} no está presente en el desplegable.", labelBuscada);
         return Boolean.FALSE;
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
    * SOBREESCRITA - PARA OBLIGAR UNA ESPERA FORZOSA. ES GENERICA A CUALQUIER ACCION Y SE CONFIGURA EN EL .properties
    * (Por Defecto irá destactivada.)
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

   /**
    * Escribe texto.
    *
    * @param testObject
    *           valor para: test object
    * @param texto
    *           valor para: texto
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   @Override
   public void escribeTexto(By testObject, String texto) throws PruebaAceptacionExcepcion {
      this.debug("escribeTexto->" + testObject.toString() + ". Texto=" + texto);
      boolean conseguido = false;
      Exception excepcion = null;
      for (int i = 1; !conseguido && i <= WebElementWrapper.NUMERO_MAXIMO_INTENTOS; i++) {
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
            this.esperaIncondicionalMilisegundos(150); // Mini espera, a veces el sendKeys de la siguiente linea se pisa
                                                       // con el asigna texto anterior y cuando se escribe una fecha da
                                                       // error de formato
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

   public void esperarFinDeProcesamiento(By byBoton, int seconds) {

      try {
         WebElement boton = WebDriverFactoryPrimeFace.getDriver().findElement(byBoton);
         this.esperaIncondicional(1);
         WebDriverWait wait = new WebDriverWait(WebDriverFactoryPrimeFace.getDriver(), Duration.ofSeconds(seconds));
         // Esperar hasta que el botón deje de tener la clase 'ui-state-loading'
         wait.until((WebDriver d) -> {
            String clase = boton.getAttribute("class");
            return !clase.contains("ui-state-loading");
         });
      }
      catch (Exception e) {
         this.warning(this.mensajeDeError(e));
      }
   }

   public void esperarFinDeProcesamiento(By byBoton) {
      this.esperarFinDeProcesamiento(byBoton, 60);
   }

}
