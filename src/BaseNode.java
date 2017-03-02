// $Id: BaseNode.java,v 1.8 2001/09/26 19:36:43 ctl Exp $ D
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

/**
 *  Node in a base tree matched witch is matched to two branches. In addition to
 *  the functionality provided by the node class, BaseNode adds matchings. Each
 *  BaseNode can be matched to multipleBranchNodes. Matches to the node in the
 *  left and right branches are accesed with the {@link #getLeft() getLeft} and
 *  {@link #getRight() getRight} methods.
 */

public class BaseNode extends Node {

  // Left and right matches
  private MatchedNodes left=null;
  private MatchedNodes right=null;

  public BaseNode( XMLNode aContent ) {
    super();
    left = new MatchedNodes(this);
    right = new MatchedNodes(this);
    content = aContent;
  }

  public BaseNode getChild( int ix ) {
    return (BaseNode) children.elementAt(ix);
  }

  public BaseNode getParent() {
    return (BaseNode) parent;
  }

  public MatchedNodes getLeft() {
    return left;
  }

  public MatchedNodes getRight() {
    return right;
  }

  public void swapLeftRightMatchings() {
    MatchedNodes t = left;
    left=right;
    right=t;
  }

}