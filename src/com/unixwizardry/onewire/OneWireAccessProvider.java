/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unixwizardry.onewire;

/**
 *
 * @author Bruce
 */

/*---------------------------------------------------------------------------
 * Copyright (C) 1999-2005 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

// OneWireAccessProvider.java

// imports
import java.util.Enumeration;
import com.unixwizardry.onewire.adapter.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;


/**
 * The OneWireAccessProvider class manages the Dallas Semiconductor
 * adapter class derivatives of <code>DSPortAdapter</code>.  An enumeration of all
 * available adapters can be accessed through the
 * member function <code>EnumerateAllAdapters</code>.  This enables an
 * application to be adapter independent. There are also facilities to get a system
 * appropriate default adapter/port combination.<p>
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> <H4> Example 1</H4>
 * Get an instance of the default 1-Wire adapter.  The adapter will be ready
 * to use if no exceptions are thrown.
 * <PRE> <CODE>
 *  try
 *  {
 *     DSPortAdapter adapter = OneWireAccessProvider.getDefaultAdapter();
 *
 *     System.out.println("Adapter: " + adapter.getAdapterName() + " Port: " + adapter.getPortName());
 *
 *     // use the adapter ...
 *
 *  }
 *  catch(Exception e)
 *  {
 *     System.out.println("Default adapter not present: " + e);
 *  }
 * </CODE> </PRE>
 * </DL>
 *
 * <DL>
 * <DD> <H4> Example 2</H4>
 * Enumerate through the available adapters and ports.
 * <PRE> <CODE>
 *  DSPortAdapter adapter;
 *  String        port;
 *
 *  // get the adapters
 *  for (Enumeration adapter_enum = OneWireAccessProvider.enumerateAllAdapters();
 *                                  adapter_enum.hasMoreElements(); )
 *  {
 *     // cast the enum as a DSPortAdapter
 *     adapter = ( DSPortAdapter ) adapter_enum.nextElement();
 *
 *     System.out.print("Adapter: " + adapter.getAdapterName() + " with ports: ");
 *
 *     // get the ports
 *     for (Enumeration port_enum = adapter.getPortNames();
 *             port_enum.hasMoreElements(); )
 *     {
 *        // cast the enum as a String
 *        port = ( String ) port_enum.nextElement();
 *
 *        System.out.print(port + " ");
 *     }
 *
 *     System.out.println();
 *  }
 * </CODE> </PRE>
 * </DL>
 *
 * <DL>
 * <DD> <H4> Example 3</H4>
 * Display the default adapter name and port without getting an instance of the adapter.
 * <PRE> <CODE>
 *  System.out.println("Default Adapter: " +
 *                      OneWireAccessProvider.getProperty("onewire.adapter.default"));
 *  System.out.println("Default Port: " +
 *                      OneWireAccessProvider.getProperty("onewire.port.default"));
 * </CODE> </PRE>
 * </DL>
 *
 * @see com.unixwizardry.onewire.adapter.DSPortAdapter
 *
 * @version    0.00, 30 August 2000
 * @author     DS
 */
public class OneWireAccessProvider {

   /**
    * Override adapter variables
    */
   private static boolean useOverrideAdapter = false;
   private static DSPortAdapter overrideAdapter = null;

   /**
    * System Version String
    */
   private static final String j2me_OWAPI_version = ".01";


   /**
    * Don't allow anyone to instantiate.
    */
   private OneWireAccessProvider ()
   {
   }

   /**
    * Returns a version string, representing the release number on official releases,
    * or release number and release date on incrememental releases.
    *
    * @return Current OneWireAPI version
    */
   public static String getVersion()
   {
      return j2me_OWAPI_version;
   }

  
   /**
    * Gets an <code>Enumeration</code> of all 1-Wire
    * adapter types supported.  Using this enumeration with the port enumeration for
    * each adapter, a search can be done to find all available hardware adapters.
    *
    * @return  <code>Enumeration</code> of <code>DSPortAdapters</code> in the system
    */
   public static Enumeration enumerateAllAdapters () {
      ArrayList         adapter_list = new ArrayList(3);
      //get the Enumeration object
      Enumeration adapters = null; 
      DSPortAdapter     adapter_instance;
      Class             adapter_class;
      String            class_name = null;     

      // check for override
      if (useOverrideAdapter)
      {
         adapter_list.add(overrideAdapter);
         adapters = Collections.enumeration(adapter_list);
         return (adapters);
      }
      
      // get the network adapter
      try
      {
         adapter_class    = Class.forName("com.unixwizardry.onewire.adapter.NetAdapter");
         adapter_instance = ( DSPortAdapter ) adapter_class.newInstance();
         adapter_list.add(adapter_instance);
      }
      catch (java.lang.NoClassDefFoundError e)
      {
         System.err.println(
            "Warning: Could not load NetAdapter: " + e);
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
      {
         // DRAIN
      }

      // get adapters from property file with keys 'onewire.register.adapter0-15'
      

      // check for no adapters
      if (adapter_list.isEmpty())
         System.err.println("No 1-Wire adapter classes found");

      return (adapters);
   }

   /**
    * Finds, opens, and verifies the specified adapter on the
    * indicated port.
    *
    * @param adapterName string name of the adapter (match to result
    *             of call to getAdapterName() method in DSPortAdapter)
    * @param portName string name of the port used in the method
    *             selectPort() in DSPortAdapter
    *
    * @return  <code>DSPortAdapter</code> if adapter present
    *
    * @throws OneWireIOException when communcation with the adapter fails
    * @throws OneWireException when the port or adapter not present
    */
   public static DSPortAdapter getAdapter (String adapterName, String portName) 
           throws OneWireIOException, OneWireException
   {
      DSPortAdapter adapter, found_adapter = null;

      // check for override
      if (useOverrideAdapter)
         return overrideAdapter;

      // enumerature through available adapters to find the correct one
      for (Enumeration adapter_enum = enumerateAllAdapters();
              adapter_enum.hasMoreElements(); )
      {
         // cast the enum as a DSPortAdapter
         adapter = ( DSPortAdapter ) adapter_enum.nextElement();

         // see if this is the type of adapter we want
         if ((found_adapter != null) ||
            (!adapter.getAdapterName().equals(adapterName)))
         {
            // not this adapter, then just cleanup
            try
            {
               adapter.freePort();
            }
            catch (Exception e)
            {
               // DRAIN
            }
            continue;
         }

         // attempt to open and verify the adapter
         if (adapter.selectPort(portName))
         {
            adapter.beginExclusive(true);

            try
            {
               // check for the adapter
               if (adapter.adapterDetected())
                  found_adapter = adapter;
               else
               {

                  // close the port just opened
                  adapter.freePort();

                  throw new OneWireException("Port found \"" + portName
                                             + "\" but Adapter \"" + adapterName
                                             + "\" not detected");
               }
            }
            finally
            {
               adapter.endExclusive();
            }
         }
         else
            throw new OneWireException(
               "Specified port \"" + portName
               + "\" could not be selected for adapter \"" + adapterName
               + "\"");
      }

      // if adapter found then return it
      if (found_adapter != null)
         return found_adapter;

      // adapter by that name not found
      throw new OneWireException("Specified adapter name \"" + adapterName
                                 + "\" is not known");
   }

   /**
    * Finds, opens, and verifies the default adapter and
    * port.  Looks for the default adapter/port in the following locations:
    * <p>
    * <ul>
    * <li> Use adapter/port in System.properties for onewire.adapter.default,
    *      and onewire.port.default properties tags.</li>
    * <li> Use adapter/port from onewire.properties file in current directory
    *      or < java.home >/lib/ (Desktop) or /etc/ (TINI)</li>
    * <li> Use smart default
    *      <ul>
    *      <li> Desktop
    *           <ul>
    *           <li> First, TMEX default (Win32 only)
    *           <li> Second, if TMEX not present, then DS9097U/(first serial port)
    *           </ul>
    *      <li> TINI, TINIExternalAdapter on port serial1
    *      </ul>
    * </ul>
    *
    * @return  <code>DSPortAdapter</code> if default adapter present
    *
    * @throws OneWireIOException when communcation with the adapter fails
    * @throws OneWireException when the port or adapter not present
    */
   public static DSPortAdapter getDefaultAdapter () throws OneWireIOException, OneWireException
   {
      if (useOverrideAdapter)
      {
          return overrideAdapter;
      }

      
      return getAdapter(getProperty("onewire.adapter.default"),
                        getProperty("onewire.port.default"));
   }

   /**
    * Gets the specfied onewire property.
    * Looks for the property in the following locations:
    * <p>
    * <ul>
    * <li> In System.properties
    * <li> In onewire.properties file in current directory
    *      or < java.home >/lib/ (Desktop) or /etc/ (TINI)
    * <li> 'smart' default if property is 'onewire.adapter.default'
    *      or 'onewire.port.default'
    * </ul>
    *
    * @param propName string name of the property to read
    *
    * @return  <code>String</code> representing the property value or <code>null</code> if
    *          it could not be found (<code>onewire.adapter.default</code> and
    *          <code>onewire.port.default</code> may
    *          return a 'smart' default even if property not present)
    */
   public static String getProperty (String propName)
   {
      try
      {
        if (useOverrideAdapter)
        {
            if (propName.equals("onewire.adapter.default"))
                return overrideAdapter.getAdapterName();
            if (propName.equals("onewire.port.default"))
                return overrideAdapter.getPortName();
        }
      }
      catch(Exception e)
      {
        //just drain it and let the normal method run...
      }

      //Properties      onewire_properties = new Properties();
      //FileInputStream prop_file          = null;
      String          property_str            = null;
      //DSPortAdapter   adapter_instance;
      //Class           adapter_class;

      // try system properties
      try
      {
         property_str = System.getProperty(propName, null);
      }
      catch (Exception e)
      {
         property_str = null;
      }

      // if defaults not found then try onewire.properties file
      //if (ret_str == null) {

         // loop to attempt to open the onewire.properties file in two locations
         // .\onewire.properties or <java.home>\lib\onewire.properties
         //String path = new String("");

         //for (int i = 0; i <= 1; i++) {

            // attempt to open the onewire.properties file
            //try
            //{
            //   prop_file = new FileInputStream(path + "onewire.properties");
            //}
            //catch (Exception e)
            //{
            //   prop_file = null;
            //}

            // if open, then try to read value
            //if (prop_file != null)
            //{

               // attempt to read the onewire.properties
               //try
               //{
               //   onewire_properties.load(prop_file);

               //   ret_str = onewire_properties.getProperty(propName, null);
               //}
               //catch (Exception e)
               //{
               //   ret_str = null;
               //}
            //}

            // check to see if we now have the value
            //if (ret_str != null)
               //break;

            // try the second path
            //path = System.getProperty("java.home") + File.separator + "lib"
            //       + File.separator;
         //}
      //}

      // if defaults still not found then check TMEX default
      //if (ret_str == null)
      //{
      //   try
      //   {
      //      if (propName.equals("onewire.adapter.default"))
      //         ret_str = TMEXAdapter.getDefaultAdapterName();
      //      else if (propName.equals("onewire.port.default"))
      //         ret_str = TMEXAdapter.getDefaultPortName();

            // if did not get real string then null out
      //     if (ret_str != null)
      //      {
      //         if (ret_str.length() <= 0)
      //            ret_str = null;
      //      }
      //   }
      //   catch (Exception e)
      //   {
            // DRAIN
      //   }
      //   catch (Error e)
      //   {
            // DRAIN
      //   }

      //}

      // if STILL not found then just pick DS9097U on 'smartDefaultPort'
      //if (ret_str == null)
      //{
      //   if (propName.equals("onewire.adapter.default"))
      //      ret_str = "DS9097U";
      //   else if (propName.equals("onewire.port.default"))
      //   {
      //      try
      //      {
      //         adapter_class    =
      //            Class.forName("com.unixwizardry.onewire.adapter.USerialAdapter");
      //         adapter_instance = ( DSPortAdapter ) adapter_class.newInstance();

               // check if has any ports (common javax.comm problem)
      //         if (adapter_instance.getPortNames().hasMoreElements())
      //            ret_str =
      //               ( String ) adapter_instance.getPortNames().nextElement();
      //      }
      //      catch (Exception e)
      //      {
      //         // DRAIN
      //      }
      //      catch (Error e)
      //      {
      //         // DRAIN
      //      }
      //   }
      //}

      //return ret_str;
   //}

   
   return property_str;
   
}

/**
    * Sets an overriding adapter.  This adapter will be returned from
    * getAdapter and getDefaultAdapter despite what was requested.
    *
    * @param adapter adapter to be the override
    *
    * @see    #getAdapter
    * @see    #getDefaultAdapter
    * @see    #clearUseOverridingAdapter
    */
   public static void setUseOverridingAdapter(DSPortAdapter adapter)
   {
        useOverrideAdapter = true;
        overrideAdapter = adapter;
   }
   
   /**
    * Clears the overriding adapter.  The operation of
    * getAdapter and getDefaultAdapter will be returned to normal.
    *
    * @see    #getAdapter
    * @see    #getDefaultAdapter
    * @see    #setUseOverridingAdapter
    */
   public static void clearUseOverridingAdapter()
   {
        useOverrideAdapter = false;
        overrideAdapter = null;
   }
}
