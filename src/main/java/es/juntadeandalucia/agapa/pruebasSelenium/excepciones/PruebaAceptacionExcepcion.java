package es.juntadeandalucia.agapa.pruebasSelenium.excepciones;

/**
 * La Class PruebaAceptacionExcepcion.
 *
 * @author AGAPA
 */
public class PruebaAceptacionExcepcion extends Exception {

   private static final long serialVersionUID = 8489944315807865157L;

   /**
    * Instancia un nuevo objeto de la clase prueba aceptacion excepcion.
    */
   public PruebaAceptacionExcepcion() {
      super();
   }

   /**
    * Instancia un nuevo objeto de la clase prueba aceptacion excepcion.
    *
    * @param arg0
    *           valor para: arg0
    */
   public PruebaAceptacionExcepcion(String arg0) {
      super(arg0);
   }
}
