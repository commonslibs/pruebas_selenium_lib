package es.juntadeandalucia.agapa.pruebasSelenium.utilidades;

import es.juntadeandalucia.agapa.pruebasSelenium.excepciones.PruebaAceptacionExcepcion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;


/**
 * Clase que contiene las conexiones a base de datos.
 *
 * @author AGAPA
 */
@Slf4j
public class BaseDeDatos {

   /**
    * Open and return a conexion to database.
    *
    * @param bd
    *           valor para: bd
    * @param host
    *           valor para: host
    * @param puerto
    *           valor para: puerto
    * @param usuario
    *           valor para: usuario
    * @param contrasena
    *           valor para: contrasena
    * @return an instance of java.sql.Connection
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public static Connection conectar(String bd, String host, String puerto, String usuario, String contrasena)
         throws PruebaAceptacionExcepcion {
      Connection conexion = null;

      String cadenaDeConexion = "jdbc:oracle:thin:@" + host + ":" + puerto + "/" + bd;

      try {
         conexion = DriverManager.getConnection(cadenaDeConexion, usuario, contrasena);
      }
      catch (SQLException e) {
         log.error("Error al conectarse a la base de datos. " + e);
         throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
      }

      return conexion;
   }

   /**
    * execute a SQL query on database.
    *
    * @param conexion
    *           valor para: conexion
    * @param queryString
    *           SQL query string
    * @return a reference to returned data collection, an instance of java.sql.ResultSet
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public static ResultSet executeQuery(Connection conexion, String queryString) throws PruebaAceptacionExcepcion {
      Assert.assertNotNull(conexion);

      Statement stm = null;

      ResultSet rs = null;
      try {
         stm = conexion.createStatement();

         rs = stm.executeQuery(queryString);

         stm.closeOnCompletion();
      }
      catch (SQLException e) {
         log.error("Error al ejecutar consulta." + queryString + ". Error SQL: " + e.getErrorCode() + "-" + e.getLocalizedMessage(), e);
         throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
      }
      return rs;
   }

   /**
    * Desconectar.
    *
    * @param conexion
    *           valor para: conexion
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public static void desconectar(Connection conexion) throws PruebaAceptacionExcepcion {
      try {
         if (conexion != null && !conexion.isClosed()) {
            conexion.close();
         }
      }
      catch (SQLException e) {
         log.error("Error al cerrar la base de datos", e);
         throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
      }
   }

   /**
    * Execute non-query (usually INSERT/UPDATE/DELETE/COUNT/SUM...) on database
    *
    * @param conexion
    *           valor para: conexion
    * @param queryString
    *           a SQL statement
    * @return single value result of SQL statement
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public static boolean execute(Connection conexion, String queryString) throws PruebaAceptacionExcepcion {
      Assert.assertNotNull(conexion);

      Statement stm = null;

      boolean result = false;
      try {
         stm = conexion.createStatement();

         result = stm.execute(queryString);

         stm.close();
      }
      catch (SQLException e) {
         log.error("Error al ejecutar consulta." + queryString + ". Error SQL: " + e.getErrorCode() + "-" + e.getLocalizedMessage(), e);
         throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
      }
      return result;
   }

   /**
    * Execute update.
    *
    * @param conexion
    *           valor para: conexion
    * @param queryString
    *           valor para: query string
    * @return result set
    * @throws PruebaAceptacionExcepcion
    *            la prueba aceptacion excepcion
    */
   public static ResultSet executeUpdate(Connection conexion, String queryString) throws PruebaAceptacionExcepcion {
      Assert.assertNotNull(conexion);

      Statement stm = null;

      ResultSet rs = null;
      try {
         stm = conexion.createStatement();

         stm.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);
         rs = stm.getGeneratedKeys();

         stm.closeOnCompletion();
      }
      catch (SQLException e) {
         log.error("Error al ejecutar consulta." + queryString + ". Error SQL: " + e.getErrorCode() + "-" + e.getLocalizedMessage(), e);
         throw new PruebaAceptacionExcepcion(e.getLocalizedMessage());
      }
      return rs;
   }
}
