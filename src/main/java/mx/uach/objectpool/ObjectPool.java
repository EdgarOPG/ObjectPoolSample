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
 * @author edgar
 */

public abstract class ObjectPool<T> {
  private long expirationTime;

  //Se crean dos Hashtables: locked y unlocked, cada una de ellas almacena un
  //Objeto Long y una key generica para recuperarlo.
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
    //Si Hashtable tiene contenido
    if (unlocked.size() > 0) {
      //Enumeration sirve para recorrer la Hashtable, se utiliza una generica
      //porque las keys son genericas tambien.
      Enumeration<T> e = unlocked.keys();
      //Mietras la enumeracion tenga elementos
      while (e.hasMoreElements()) {
        t = e.nextElement();
        if ((now - unlocked.get(t)) > expirationTime) {
          // object has expired
          unlocked.remove(t);
          expire(t);
          t = null;
        } else {
          if (validate(t)) {
            unlocked.remove(t);
            locked.put(t, now);
            return (t);
          } else {
            // object failed validation
            unlocked.remove(t);
            expire(t);
            t = null;
          }
        }
      }
    }
    // no objects available, create a new one
    t = create();
    locked.put(t, now);
    return (t);
  }

  public synchronized void checkIn(T t) {
    locked.remove(t);
    unlocked.put(t, System.currentTimeMillis());
  }
}

//The three remaining methods are abstract 
//and therefore must be implemented by the subclass

