/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.uach.objectpool;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author sourcemaking
 * https://sourcemaking.com/design_patterns/object_pool/java
 */

public abstract class ObjectPool<T> {
  private long expirationTime;

  //Se crean dos Hashtables: locked y unlocked, cada una de ellas almacena un
  //Objeto Long que es el inicio del ciclo de vida del objeto y una key que es
  //Objeto que se quiere guardar
  private Hashtable<T, Long> locked, unlocked;

  public ObjectPool() {
    //Se define un tiempo de expiracion.
    expirationTime = 30000; // 30 seconds
    //Se crean instancias de cada una de las tablas declaradas arriba.
    locked = new Hashtable<T, Long>();
    unlocked = new Hashtable<T, Long>();
  }

  //Al ser metodos abstractos se declaran sin cuerpo, este solo se define cuando
  //se sobreescriben a traves de otra clase que extienda a ObjectPool
  protected abstract T create();

  public abstract boolean validate(T obj);

  public abstract void expire(T obj);

  public synchronized T checkOut() {
    //Este es el tiempo actual en milisegundos
    long now = System.currentTimeMillis();
    //T es de la misma clase que el objeto que implementa a ObjectPool, aqui se
    T t;
    //Si el Hashtable de objetos en uso tiene contenido
    if (unlocked.size() > 0) {
      //Enumeration sirve para recorrer la Hashtable, se utiliza una generica
      //porque las keys son genericas tambien.
      Enumeration<T> e = unlocked.keys();
      //Mietras la enumeracion tenga elementos
      while (e.hasMoreElements()) {
        //t va a tomar el valor de cada elemento key
        t = e.nextElement();
        //Si now menos el elemento con la key t es mayor al tiempo de espera 
        if ((now - unlocked.get(t)) > expirationTime) {
          // El objeto expiro y se saca de la hastable de en uso.
          unlocked.remove(t);
          expire(t);
          t = null;
        } else {
          if (validate(t)) {
            //Si el objeto aun tiene tiempo de vida, se valida y si pasa la 
            //validacion se pone de la tabla de en uso y de regreso en la 
            //de espera
            unlocked.remove(t);
            locked.put(t, now);
            return (t);
          } else {
            //Si el objeto falla la validacion se deshecha
            unlocked.remove(t);
            expire(t);
            t = null;
          }
        }
      }
    }
    // Cuando no hay objetos disponibles se crea uno nuevo y se pone en espera.
    t = create();
    locked.put(t, now);
    return (t);
  }

//Devolucion de un 
  public synchronized void checkIn(T t) {
    locked.remove(t);
    unlocked.put(t, System.currentTimeMillis());
  }
}

//The three remaining methods are abstract 
//and therefore must be implemented by the subclass

