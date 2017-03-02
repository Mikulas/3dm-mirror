// $Id: Diff.java,v 1.5 2001/09/26 19:36:44 ctl Exp $ D
//
// Copyright (c) 2001, Tancred Lindholm <ctl@cs.hut.fi>
//
// This file is part of 3DM.
//
// 3DM is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// 3DM is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with 3DM; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;

/** Produces the diff between two naturally matched trees.
 *  Collapsing multiple copy-ops using the run attribute is not implemented in
 *  this version.
 */

public class Diff {

  private Matching m = null;
  private static final Attributes EMPTY_ATTS = new AttributesImpl();
  private static final Set RESERVED;
  private static final String DIFF_NS ="diff:";
  static {
      RESERVED = new HashSet();
      RESERVED.add(DIFF_NS+"copy");
      RESERVED.add(DIFF_NS+"insert");
      RESERVED.add(DIFF_NS+"esc");
  }

  /** Construct a diff operating on the matched trees passed to the constructor.
   *  Note that the matching contains pointers to the base and new trees.
   *  @param am Matching between trees to diff
   */
  public Diff(Matching am) {
    m=am;
  }

  /** Encode the diff between the trees passed to the constructor.
   *  @param ch Output encoder for the diff
   */
  public void diff( ContentHandler ch ) throws SAXException {
    enumerateNodes(m.getBaseRoot(),nodeNumbers);
    ch.startDocument();
    ch.startElement("","","diff",EMPTY_ATTS);
    copy( m.getBaseRoot(), m.getBranchRoot(), ch );
    ch.endElement("","","diff");
    ch.endDocument();
  }

  protected void copy( BaseNode b, BranchNode branch, ContentHandler ch )
                        throws SAXException {
    // Find stopnodes
    Vector stopNodes = new Vector();
    m.getAreaStopNodes( stopNodes, branch );
    for( Iterator i = stopNodes.iterator();i.hasNext();) {
      BranchNode stopNode = (BranchNode) i.next();
      String dst = getId(stopNode.getBaseMatch());
      for( int ic = 0;ic < stopNode.getChildCount();ic++) {
        BranchNode child = stopNode.getChild(ic);
        if( child.hasBaseMatch() ) {
          // Copy tree...
          AttributesImpl copyAtts = new AttributesImpl();
          copyAtts.addAttribute("","","src","CDATA",getId(child.getBaseMatch()));
          copyAtts.addAttribute("","","dst","CDATA",dst);
          ch.startElement("","",DIFF_NS+"copy",copyAtts);
          copy( child.getBaseMatch(), child, ch );
          ch.endElement("","",DIFF_NS+"copy");
        } else {
          // Insert tree...
          AttributesImpl copyAtts = new AttributesImpl();
          copyAtts.addAttribute("","","dst","CDATA",dst);
          ch.startElement("","",DIFF_NS+"insert",copyAtts);
          insert( child, ch );
          ch.endElement("","",DIFF_NS+"insert");
        }
      } // endfor children
    }
  }

  protected void insert( BranchNode branch, ContentHandler ch )
                          throws SAXException {
    XMLNode content = branch.getContent();
    if( content instanceof XMLTextNode ) {
      XMLTextNode ct = (XMLTextNode) content;
      ch.characters(ct.getText(),0,ct.getText().length);
    } else {
      // Element node
      XMLElementNode ce = (XMLElementNode) content;
      boolean escape = RESERVED.contains(ce.getQName());
      if( escape ) ch.startElement("","",DIFF_NS+"esc",EMPTY_ATTS);
      ch.startElement(ce.getNamespaceURI(),ce.getLocalName(),ce.getQName(),ce.getAttributes());
      if( escape ) ch.endElement("","",DIFF_NS+"esc");
      for( int i=0;i<branch.getChildCount();i++) {
        BranchNode child = branch.getChild(i);
        if( child.hasBaseMatch() ) {
          AttributesImpl copyAtts = new AttributesImpl();
          copyAtts.addAttribute("","","src","CDATA",
            getId(child.getBaseMatch()));
          ch.startElement("","",DIFF_NS+"copy",copyAtts);
          copy( child.getBaseMatch(), child, ch );
          ch.endElement("","",DIFF_NS+"copy");
        } else
          insert( child, ch );
      }
      if( escape ) ch.startElement("","",DIFF_NS+"esc",EMPTY_ATTS);
      ch.endElement(ce.getNamespaceURI(),ce.getLocalName(),ce.getQName());
      if( escape ) ch.endElement("","",DIFF_NS+"esc");
    }
  }

  protected Map nodeNumbers = new HashMap();

  // Get BFS number of node
  protected String getId( Node n ) {
    return ((Integer) nodeNumbers.get(n)).toString();
  }

  // BFS Enumeration of nodes. Useful beacuse adjacent nodes have subsequent ids
  // => diff can use the "run" attribute more often
  protected void enumerateNodes( Node start, Map map ) {
    int id = 0;
    LinkedList queue = new LinkedList();
    queue.add(start);
    while( !queue.isEmpty() ) {
      Node n = (Node) queue.removeFirst();
      map.put(n,new Integer(id));
      for( int i=0;i<n.getChildCount();i++)
        queue.add(n.getChildAsNode(i));
      id++;
    }
  }
}