// $Id: XMLPrinter.java,v 1.2 2001/06/20 13:25:59 ctl Exp $

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

class XMLPrinter extends DefaultHandler {


  int indent = 0;
  private static final String IND =
  "                                                                              ";
  private java.io.PrintWriter pw = null;

  XMLPrinter( java.io.PrintWriter apw ) {
    pw=apw;
  }
   ////////////////////////////////////////////////////////////////////
   // Event handlers.
   ////////////////////////////////////////////////////////////////////


   public void startDocument ()
   {
      childcounter =HAS_CONTENT;
      pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
   }


   public void endDocument ()
   {
       //System.out.println("End document");
      pw.flush();
   }

    java.util.Stack csstack = new java.util.Stack();
   Integer childcounter = null;
   public void startElement (String uri, String name,
                             String qName, Attributes atts)

   {
    if( childcounter == null ) {
       pw.println(">");
      childcounter =HAS_CONTENT;
    }
    StringBuffer tagopen = new StringBuffer();
    tagopen.append('<');
    tagopen.append( qName );
  //    tagopen.append(' ');
    if( atts != null && atts.getLength() != 0 ) {
      for( int i = 0;i<atts.getLength();i++ ) {
        tagopen.append(' ');
        tagopen.append(atts.getQName(i));
        tagopen.append('=');
        tagopen.append('"');
        tagopen.append(atts.getValue(i));
        tagopen.append('"');
      }
    }
    csstack.push( childcounter );
    childcounter = null;
//      if( assumeNoChildren )
//        tagopen.append("/>");
//      else
//        tagopen.append('>');
    pw.print(IND.substring(0,indent)  + tagopen.toString());
    indent ++;
   }


   public void endElement (String uri, String name, String qName)
   {
      indent--;
        if( childcounter == null )
          pw.println(" />");
        else
          pw.println(IND.substring(0,indent)+ "</"+qName+">");
      childcounter = (Integer) csstack.pop();
   }

   final Integer HAS_CONTENT = new Integer(0);

   public void characters (char ch[], int startpos, int length)
   {
      if(childcounter!=HAS_CONTENT)
         pw.println(">");
      childcounter = HAS_CONTENT;
      StringBuffer sb = new StringBuffer();
      for(int i=startpos;i<startpos+length;i++) {
        switch( ch[i] ) {
          case '<': sb.append("&lt;");
                break;
          case '>': sb.append(">");
                break;
          case '\'': sb.append("&apos;");
                break;
          case '&': sb.append("&amp;");
                break;
          case '"': sb.append("&quot;");
                break;
          default:
              sb.append(ch[i]);
        }
      }
      String chars = sb.toString();
//        String chars = new String( ch, startpos, length ).trim();
      if( chars.length() == 0 )
        return;/*
      int start=0,next=-1;
      do {
        next=chars.indexOf("\n",start);
        if( next==-1)
          pw.println(chars.substring(start));
        else {
          pw.println(chars.substring(start,next));
          start=next+1;
        }
      } while( next != -1 );*/
      pw.println(chars);
      //System.err.println("OUT:"+chars);
   }


}
