// $Id: TreeDiffMerge.java,v 1.5 2001/09/26 19:36:46 ctl Exp $ D
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

import gnu.getopt.*;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PrintStream;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

/** Driver class for 3DM. Parses command line and runs the merge/diff/patch
 *  algorithms.
 */
public class TreeDiffMerge {
  public static void main(String[] args) throws java.io.IOException {
    System.err.println(
    "3DM XML Tree Differencing and Merging Tool. PROTOTYPE: $Revision: 1.5 $" );
    // Get command line options
    int firstFileIx = parseOpts( args );
    if( op == MERGE && (args.length - firstFileIx) == 3 )
      merge( firstFileIx, args, System.out );
    else if( op == MERGE && (args.length - firstFileIx) == 4 )
      merge( firstFileIx, args, new FileOutputStream(args[firstFileIx+3]));
    else if( op == DIFF && (args.length - firstFileIx) == 2 )
      diff( firstFileIx, args, System.out );
    else if( op == DIFF && (args.length - firstFileIx) == 3 )
      diff( firstFileIx, args, new FileOutputStream(args[firstFileIx+2]));
    else if( op == PATCH && (args.length - firstFileIx) == 2 )
      patch( firstFileIx, args, System.out );
    else if( op == PATCH && (args.length - firstFileIx) == 3 )
      patch( firstFileIx, args, new FileOutputStream(args[firstFileIx+2]));
    else {
      System.err.println("Usage: 3dm [options] {-m base branch1 branch2|-d "+
      "base branch1 |-p base patch} [outfile]" );
      System.err.println("Use the -m (or --merge) option to merge the files "+
      "base, branch1 and branch2"  );
      System.err.println("Use the -d (or --diff) option to diff the files "+
      "base and branch1"  );
      System.err.println("Use the -p (or --patch) option to patch the file "+
      "base with the file patch"  );
      System.err.println("The options are:");
      System.err.println("-e, --editlog[=logfile]");
      System.err.println("   Log edit operations to logfile, default edit.log");
      System.err.println("-c, --copythreshold=bytes");
      System.err.println("   Threshold for considering a duplicate structure "+
                          "to be a copy. Default value is " +
                          Matching.COPY_THRESHOLD + " bytes");
    }
  }

  // Factory for BaseNode:s
  private static NodeFactory baseNodeFactory =  new NodeFactory() {
            public Node makeNode(  XMLNode content ) {
              return new BaseNode( content  );
            }
        };

  // Factory for BranchNode:s
  private static NodeFactory branchNodeFactory =  new NodeFactory() {
            public Node makeNode(  XMLNode content ) {
              return new BranchNode(  content  );
            }
        };

  /** Runs merge algorithm.
   *  @param ix Index in args of first file
   *  @param out Outputstream for merged tree */
  protected static void merge( int ix, String[] args, OutputStream out ) {
    BaseNode docBase=null;
    BranchNode docA=null,docB=null;
    String currentFile = "";
    try {
      XMLParser p = new XMLParser();
      currentFile = args[ix+0];
      docBase = (BaseNode) p.parse( currentFile,baseNodeFactory);
      currentFile = args[ix+1];
      docA = (BranchNode) p.parse( currentFile, branchNodeFactory);
      currentFile = args[ix+2];
      docB = (BranchNode) p.parse( currentFile,branchNodeFactory);
    } catch ( Exception e ) {
      System.err.println("XML Parse error in " + currentFile +
                            ". Detailed exception info is:" );
      System.err.println( e.toString() );
      e.printStackTrace();
      return;
    }
    try {
      Merge merge = new Merge( new TriMatching( docA, docBase, docB ) );
//$CUT
/*      PrintWriter pw = new PrintWriter( new FileOutputStream("m.log"));
      dumpMatch(docA,pw);
      pw.println("------docb-----------");
      dumpMatch(docB,pw);
      pw.close();
*/
//$CUT
      merge.merge( new XMLPrinter( new PrintWriter( out)  ) );
      merge.getConflictLog().writeConflicts(new XMLPrinter(
        new PrintWriter( new FileOutputStream( conflictLogName ))));
      if( editLog )
        merge.getEditLog().writeEdits( new XMLPrinter(
          new PrintWriter( new FileOutputStream( editLogName ))));
    } catch ( Exception e ) {
      System.err.println("Exception while merging.. trace follows:");
      System.err.println( e.toString() );
      e.printStackTrace();
    }
  }

  /** Runs diff algorithm.
   *  @param ix Index in args of first file
   *  @param out Outputstream for merged tree */

  protected static  void diff( int ix, String[] args, OutputStream out ) {
    BaseNode docBase=null;
    BranchNode docA=null;
    String currentFile = "";
    try {
      XMLParser p = new XMLParser();
      currentFile = args[ix+0];
      docBase = (BaseNode) p.parse( currentFile,baseNodeFactory);
      currentFile = args[ix+1];
      docA = (BranchNode) p.parse( currentFile, branchNodeFactory);
    } catch ( Exception e ) {
      System.err.println("XML Parse error in " + currentFile +
                          ". Detailed exception info is:" );
      System.err.println( e.toString() );
      e.printStackTrace();
      return;
    }
    try {
      Matching m = new DiffMatching( docBase, docA );
      Diff diff = new Diff( m );
      diff.diff(new XMLPrinter( new PrintWriter(out) ));
    } catch ( Exception e ) {
      System.err.println("Exception while diffing.. trace follows:");
      System.err.println( e.toString() );
      e.printStackTrace();
    }
  }
  /** Runs patch algorithm.
   *  @param ix Index in args of first file
   *  @param out Outputstream for merged tree */
  static  void patch( int ix, String[] args, OutputStream out ) {
    BaseNode docBase=null;
    BranchNode docPatch=null;
    String currentFile = "";
    try {
      XMLParser p = new XMLParser();
      currentFile = args[ix+0];
      docBase = (BaseNode) p.parse( currentFile,baseNodeFactory);
      currentFile = args[ix+1];
      docPatch = (BranchNode) p.parse( currentFile, branchNodeFactory);
    } catch ( Exception e ) {
      System.err.println("XML Parse error in " + currentFile +
                        ". Detailed exception info is:" );
      System.err.println( e.toString() );
      e.printStackTrace();
      return;
    }
    try {
      Patch patch = new Patch();
      patch.patch(docBase,docPatch, new XMLPrinter( new PrintWriter( out  ) ) );
    } catch ( Exception e ) {
      System.err.println("Exception while diffing.. trace follows:");
      System.err.println( e.toString() );
      e.printStackTrace();
    }
  }

  // Default files
  public static final String EDITLOG = "edit.log";
  public static final String CONFLICTLOG = "conflict.log";
  // operation codes, returned by parseOpts()
  public static final int MERGE = 0;
  public static final int DIFF = 1;
  public static final int PATCH = 2;

  protected static boolean editLog = false;
  protected static String editLogName = EDITLOG;
  protected static String conflictLogName = CONFLICTLOG;
  protected static int op = -1;

  // Parse command line options.
  private static int parseOpts( String args[] ) {
    LongOpt lopts[] = {
      new LongOpt("editlog",LongOpt.OPTIONAL_ARGUMENT,null,'e'),
      new LongOpt("copythreshold",LongOpt.REQUIRED_ARGUMENT,null,'c'),
      new LongOpt("merge",LongOpt.NO_ARGUMENT,null,'m'),
      new LongOpt("diff",LongOpt.NO_ARGUMENT,null,'d'),
      new LongOpt("patch",LongOpt.NO_ARGUMENT,null,'p'),
    };
    Getopt g = new Getopt("3DM", args, "e::c:mdp", lopts);
    int c;
    String arg;
    while ((c = g.getopt()) != -1) {
     switch(c) {
          case 'e':
            editLog = true;
            editLogName = getStringArg(g,EDITLOG);
            break;
          case 'c':
            Matching.COPY_THRESHOLD = getIntArg(g,Matching.COPY_THRESHOLD);
///           //System.err.println("COPY_THRESHOLD=" + Matching.COPY_THRESHOLD);
            break;
          case 'm':
            op = MERGE;
            break;
          case 'p':
            op = PATCH;
            break;
          case 'd':
            op = DIFF;
            break;
      }
    }
    return g.getOptind();
  }

  static String getStringArg( Getopt g, String defval ) {
    String arg = g.getOptarg();
    if( arg == null || "?".equals(arg) )
      return defval;
    else
      return arg;
  }

  static int getIntArg( Getopt g, int defval ) {
    String arg = g.getOptarg();
    if( arg == null || "?".equals(arg) )
      return defval;
    else {
      try {
        return Integer.parseInt(arg);
      } catch (Exception e) {
        return defval;
      }
    }
  }

//$CUT
  // DEBUg code
  private static void dumpMatch(BranchNode n, PrintWriter pw ) {
    if( n.hasBaseMatch() )
      pw.print(n.getBaseMatchType()+":"+PathTracker.getPathString(n.getBaseMatch())+" ");
    else
      pw.print("N/A ");
    pw.println(n.getContent().toString());
    for(int i=0;i<n.getChildCount();i++)
      dumpMatch(n.getChild(i),pw);
  }
//$CUT
}