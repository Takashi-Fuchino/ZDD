import java.util.*;
import java.util.ArrayList;
import java.util.HashSet;

public class ZDD {
    protected enum State { Occupied, Empty, Deleted };

    /* ZDDの本体 */
    class ZddElement {
	int _var;
	int _right;
	int _left;
	int _refCnt;
	State _state;
	ZddElement() { 
	    _var = -1;
	    _right = -1;
	    _left = -1;
	    _refCnt = 0;
	    _state = State.Empty;
	}
	ZddElement(final int var, final int right, final int left, final int refCnt, final State state) {
	    _var = _var;
	    _right = right;
	    _left = left;
	    _refCnt = refCnt;
	    _state = state;
	}
	ZddElement(ZddElement base) { 
	    _var = base._var;
	    _right = base._right;
	    _left = base._left;
	    _refCnt = base._refCnt;
	    _state = base._state;
	}
	public int getVar() { return _var; }
	public int getRight() { return _right; }
	public int getLeft(){ return _left; }
	public int getRefCnt(){ return _refCnt; }
	public State getState(){ return _state; }
	public void setVar(final int var) { _var = var; }
	public void setRight(final int right) { _right = right; }
	public void setLeft(final int left) { _left = left; }
	public void setState(final State state) { _state = state; }
	public void setRef_cnt(final int refCnt){ _refCnt = refCnt; }
	public void incRefCnt() { ++_refCnt; }
	public void decRefCnt() { --_refCnt; }

	public void setRefCntZero() { _refCnt = 0; }
	public void setStateEmpty() { _state = State.Empty; }

	@Override
	public String toString() {
	    return "var = " + _var + ", left = " + _left + ", right = " + _right + ", refCnt = " + _refCnt + ", state = " + _state;
	}
    }

    Integer NUMBEROFNODES;
    ZddElement[] zddTable = null;
    ArrayList<Integer> decRef = null;

    void decRefcounterRec(Integer k) {
    	if (0 == k || 1 == k)
	    return;
    	zddTable[k].decRefCnt();
    	if (0 == zddTable[k].getRefCnt()) {
    	    decRefcounterRec(zddTable[k].getLeft());
    	    decRefcounterRec(zddTable[k].getRight());
    	}
    }

    void decRefcounter() {
    	for (Integer i : decRef)
    	    decRefcounterRec(i);
    }

    public boolean eqZddTableEntry(int k, int Var, int Left, int Right) {
    	if (zddTable[k]._var != Var) 
	    return false;
    	if (zddTable[k]._left != Left) 
	    return false;
    	if (zddTable[k]._right != Right)
	    return false;
    	return true;
    }

    Integer hf(int var, int l, int r) { return ((100*var)+(10*l)+r) % NUMBEROFNODES; }

    public void zddInit(final Integer size, final int n) {
	
	decRef = new ArrayList<Integer>();
	OPCACHETABLESIZE = size;
	OpCacheTable = new OpCacheElement[size];

	PATHCACHESIZE = size;
	pathCacheTable = new PathCacheElement[size];
  
    	NUMBEROFNODES = size;
    	zddTable = new ZddElement[size];

	for (int i = 0; i < size; ++i) {
	    OpCacheTable[i] = new OpCacheElement();
	    pathCacheTable[i] = new PathCacheElement();
	    zddTable[i] = new ZddElement();
	}
	
    	zddTable[0].setVar(n); zddTable[1].setVar(n);
    	zddTable[0].setLeft(-1); zddTable[1].setLeft(-1);
    	zddTable[0].setRight(-1); zddTable[1].setRight(-1);
    	zddTable[0].setState(State.Occupied); zddTable[1].setState(State.Occupied);

    	OpCacheTable[0].setState(State.Empty); OpCacheTable[1].setState(State.Empty);
    	pathCacheTable[0].setState(State.Empty); pathCacheTable[1].setState(State.Empty);
       	for (Integer i = 2; i < size; ++i) {
    	    zddTable[i].setState(State.Empty);
    	    OpCacheTable[i].setState(State.Empty);
    	    pathCacheTable[i].setState(State.Empty);
    	}
    	System.out.println("initialize the ZDD table and the operation cache table");
    }

    public int getnode(final int var, final int left, final int right) {
    	if (0 == right) { return left; }

    	int P = member(var, left, right);
    	if (-2 != P)
    	    return P;

    	incRefCounter(left);
    	incRefCounter(right);
    	P = insert(var, left, right);
    	return P;
    }

    
    public int change(final int P, final int v) {
    	if (v == zddTable[P].getVar())
    	    return getnode(v, zddTable[P].getLeft(), zddTable[P].getRight());
    	if (v < zddTable[P].getVar())
    	    return getnode(v, 0, P);
    	int R = memberOp(Op.Change, P, v);
    	if (-2 != R)
    	    return R;
    	R = getnode(zddTable[P].getVar(), change(zddTable[P].getLeft(), v), change(zddTable[P].getRight(), v));
    	insertOp(Op.Change, P, v, R);
    	return R;
    }

    int union(final int P, final int Q) {
	if (0 == P)
	    return Q;
	if (0 == Q || P == Q)
	    return P;

	int R = memberOp(Op.Union, P, Q);
	if (-2 != R)
	    return R;
	if (zddTable[P].getVar() < zddTable[Q].getVar()) {
	    R = getnode(zddTable[P].getVar(), union(zddTable[P].getLeft(), Q), zddTable[P].getRight());
	}
	if (zddTable[P].getVar() > zddTable[Q].getVar()) {
	    R = getnode(zddTable[Q].getVar(), union(P, zddTable[Q].getLeft()), zddTable[Q].getRight());
	}
	if (zddTable[P].getVar() == zddTable[Q].getVar()) {
	    R = getnode(zddTable[P].getVar(), union(zddTable[P].getLeft(), zddTable[Q].getLeft()), union(zddTable[P].getRight(), zddTable[Q].getRight()));
	}
	insertOp(Op.Union, P, Q, R);
	return R;
    }

    int intersection(final int P, final int Q) {
	if (0 == P || 0 == Q) { return 0; }
	if (P == Q) { return P; }
	int R = memberOp(Op.Intersection, P, Q);
	if (-2 != R) { return R; }
	if (zddTable[P].getVar() < zddTable[Q].getVar()) {
	    R = intersection(zddTable[P].getLeft(), Q);
	}
	if (zddTable[P].getVar() > zddTable[Q].getVar()) {
	    R = intersection(P, zddTable[Q].getLeft());
	}
	if (zddTable[P].getVar() == zddTable[Q].getVar()) {
	    R = getnode(zddTable[P].getVar(), intersection(zddTable[P].getLeft(), zddTable[Q].getLeft()), intersection(zddTable[P].getRight(), zddTable[Q].getRight()));
	}
	insertOp(Op.Intersection, P, Q, R);
	return R;
    }

    int setminus(final int P, final int Q) {
	if (0 == P || P == Q) { return 0; }
	if (0 == Q) { return P; }
	int R = memberOp(Op.Setminus, P, Q);
	if (-2 != R) { return R; }
	if (zddTable[P].getVar() < zddTable[Q].getVar()) {
	    R = getnode(zddTable[P].getVar(), setminus(zddTable[P].getLeft(), Q), zddTable[P].getRight());
	}
	if (zddTable[P].getVar() > zddTable[Q].getVar()) {
	    R = setminus(P, zddTable[Q].getLeft());
	}
	if (zddTable[P].getVar() == zddTable[Q].getVar()) {
	    R = getnode(zddTable[P].getVar(), setminus(zddTable[P].getLeft(), zddTable[Q].getLeft()), setminus(zddTable[P].getRight(), zddTable[Q].getRight()));
	}
	insertOp(Op.Setminus, P, Q, R);
	return R;
    }

    public int getFoundNumber(int var, int left, int right) {
    	int i, k, found = -1;
    	State cstate;
    	k = i = hf(var, left, right);
    	do {
    	    cstate = zddTable[k].getState();
	    
    	    if (State.Empty == cstate || State.Deleted == cstate) {
    		if (found < 0)  /* there is an empty cell */
    		    found = k;
    	    } else {
    		if (eqZddTableEntry(k, var, left, right)) /* input node already exists */
    		    return k;
    	    }
    	    k = (k+1) % NUMBEROFNODES;
    	    /* printf("collision occurs\n"); */
    	} while (State.Empty != cstate && k != i);
	
    	return found;
    }

    int insert(final int var, final int left, final int right) {
    	int found = getFoundNumber(var, left, right);

    	while (found < 0) {
    	    // System.out.println("Dictionary is full. NUM_OF_NODES = "+ NUM_OF_NODES);
    	    if (!decRef.isEmpty()) {
    		decRefcounter();
    		decRef = null;
		decRef = new ArrayList<Integer>();
    		setOpCacheTableEmpty();
    	    }
    
    	    found = getFoundNumber(var, left, right);
    	    if (found < 0) {
    		/* FIXME */
    		// System.out.println("Dictionary is truly full. NUM_OF_NODES = "+ NUM_OF_NODES);
    		increaseSlots();
    		found = getFoundNumber(var, left, right);
    		// System.out.println("found = " + found);
    	    }
    	}

    	zddTable[found].setState(State.Occupied);
    	zddTable[found].setVar(var);
    	zddTable[found].setLeft(left);
    	zddTable[found].setRight(right);

    	return found;
    }

    public void delete(final int var, final int left, final int right) {
    	int i, k;
    	State cstate;

    	k = i = hf(var, left, right);
    	do {
    	    cstate = zddTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (eqZddTableEntry(k, var, left, right)) { /* find the input node and delete it */
		    zddTable[k].setState(State.Deleted);
		    return; 
		} 
    	    }
    	    k = (k+1) % NUMBEROFNODES;
    	    /* printf("collision occurs\n"); */
    	} while (State.Empty != cstate && k != i);
    	return; /* there is no input node */
    }

    public int member(final int var, final int left, final int right) {
    	int i, k;
    	State cstate;

    	k = i = hf(var, left, right);
    	do {
    	    cstate = zddTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (eqZddTableEntry(k, var, left, right)) /* find the input node */
		    return k;
    	    }
    	    k = (k+1) % NUMBEROFNODES;
    	    /* printf("collision occurs\n"); */
    	} while (State.Empty != cstate && k != i);
    	return -2; /* there is no input node */
    }

    public void incRefCounter(final int P) { zddTable[P].incRefCnt(); }

    public ZddElement[] ZddTableCopy(Integer n){
    	ZddElement Z[] = new ZddElement[n];
    	for (Integer i = 0 ; i < n ; ++i)
    	    Z[i] = new ZddElement(zddTable[i]);
    	return Z;
    }

    public void ZddTableEntryPrint() {
    	for (Integer i = 0 ; i < NUMBEROFNODES; ++i)
	    System.out.println("zddTable[" + i + "]: " + zddTable[i]);
    }

    public void increaseZddTableSlots() {
    	ZddElement W[] = new ZddElement[NUMBEROFNODES];
    	for(Integer i = 0; i < NUMBEROFNODES; ++i)
    	    W[i] = new ZddElement(zddTable[i]);
    	NUMBEROFNODES *= 2;
	zddTable = null;
	zddTable = new ZddElement[NUMBEROFNODES];
	for (Integer i = 0; i < NUMBEROFNODES/2; ++i)
	    zddTable[i] = new ZddElement(W[i]);
    	for (Integer i = NUMBEROFNODES/2; i < NUMBEROFNODES; ++i) {
	    zddTable[i] = new ZddElement();
    	    zddTable[i].setRefCntZero();
    	    zddTable[i].setStateEmpty();
    	}
    }

    public void increaseSlots() {
	increaseZddTableSlots();
	resetOpCacheTable();
	resetPathCacheTable();
    }

    /*************** 根節点から1終端節点へのパスを列挙するためのハッシュ ***************/

    class PathCacheElement {
	int _P;
	ArrayList<String> _L;
	State _state;
	PathCacheElement() {
	    _P = 0;
	    _L = null;
	    _state = State.Empty;
	}
	PathCacheElement(final int P, final ArrayList<String> L, final State state) {
	    _P = P;
	    _L = new ArrayList<String>(L);
	    _state = state;
	}
	PathCacheElement(PathCacheElement base) {
	    System.out.println(base);
	    _P = base._P;
	    if (null != base._L)
	    	_L = new ArrayList<String>(base._L);
	    _state = base._state;
	}
	public int getP(){ return _P; }
	public ArrayList<String> getL(){ return _L; }
	public State getState(){ return _state; }
	public void setP(final int P) { _P = P; }
	public void setL(final ArrayList<String> L) { _L = new ArrayList<String>(L); }
	public void setState(final State state) { _state = state; }
    	public void setStateEmpty() { _state = State.Empty; }

	@Override
	public String toString() {
	    return "P = " + _P + ", L = " + _L + ", state = " + _state;
	}
    }

    Integer PATHCACHESIZE;
    PathCacheElement[] pathCacheTable = null;

    public Integer hfPath(final int P) { return P % PATHCACHESIZE; }

    Integer getFoundNumberPath(final int P) {
	int i, k, found = -1;
	State cstate;

	k = i = hfPath(P);
	do {
	    cstate = pathCacheTable[k].getState();
	    if (State.Empty == cstate || State.Deleted == cstate) {
		if (found < 0)
		    found = k;
	    }
	    else {
		if (pathCacheTable[k].getP() == P)
		    return k;
	    }
	    k = (k+1) % PATHCACHESIZE;
	} while (State.Empty != cstate && k != i);
	return found;
    }
    
    ArrayList<String> memberPath(final int P) {
    	int i, k;
    	State cstate;

    	k = i = hfPath(P);
    	do {
    	    cstate = pathCacheTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (pathCacheTable[k].getP() == P)
    		    return pathCacheTable[k].getL(); /* find the input node */
    	    }
    	    k = (k+1) % PATHCACHESIZE;
    	} while (State.Empty != cstate && k != i);
	
    	return null; /* there is no input node */
    }

    public void insertPath(final int P, ArrayList<String> L) {
	int found = getFoundNumberPath(P);
	while (found < 0) {
	    increasePathCacheTable();
	    found = getFoundNumberPath(P);
	}

    	pathCacheTable[found].setState(State.Occupied);
    	pathCacheTable[found].setL(L);
    }

    public void deletePath(final int P) {
    	int i, k;
    	State cstate;

    	k = i = hfPath(P);
    	do {
    	    cstate = pathCacheTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (pathCacheTable[k].getP() == P) { /* find the input node and delete it */
    		    pathCacheTable[k].setState(State.Deleted);
    		    return;
    		} 
    	    }
    	    k = (k+1) % PATHCACHESIZE; 
    	} while (State.Empty != cstate && k != i);
    	return; /* there is no input node */
    }

    void paddingCharacter(ArrayList<String> L, final char c, final int l) {
	if (null == L || 0 == l)
	    return;
	final int originLength = L.get(0).length();
	final int newLength = originLength + l;
	for (String s : L) {
	    int i;
	    int LL = newLength - originLength;
	    for (i = 0; i < LL; ++i)
		;
	    for ( ; i < newLength; ++i)
		;
	}
    }
    
    ArrayList<String> pathsTo1Sub(final int P) {
	if (0 == P)
	    return null;
	if (1 == P) {
	    ArrayList<String> E = new ArrayList<String>();
	    String e = new String("");
	    E.add(e);
	    return E;
	}

	ArrayList<String> ss = memberPath(P);
	if (null != ss) {
	    ArrayList<String> L = new ArrayList<String>(ss);
	    return ss;
	}

	ArrayList<String> LL = pathsTo1Sub(zddTable[P].getLeft());
	paddingCharacter(LL, '0', zddTable[zddTable[P].getLeft()].getVar()-zddTable[P].getVar()-1);
	paddingCharacter(LL, '0', 1);


	ArrayList<String> LR = pathsTo1Sub(zddTable[P].getRight());
	paddingCharacter(LR, '0', zddTable[zddTable[P].getRight()].getVar()-zddTable[P].getVar()-1);
	paddingCharacter(LR, '1', 1);

	ss = listStringsCopyConcat(LL, LR);
	insertPath(P, ss);

	return ss;	
    }

    ArrayList<String> listStringsCopyConcat(ArrayList<String> L1, ArrayList<String> L2) {
	ArrayList<String> L = new ArrayList<String>(L1);
	for (String s : L2)
	    L.add(s);
	return L;
    }
    
    ArrayList<String> pathsTo1(final int P) {
	ArrayList<String> S = pathsTo1Sub(P);
	paddingCharacter(S, '0', zddTable[P].getVar());
	return S;
    }
    
    void increasePathCacheTable() {
	PathCacheElement Q[] = new PathCacheElement[PATHCACHESIZE];
	for (Integer i = 0; i < PATHCACHESIZE; ++i)
	    Q[i] = new PathCacheElement(pathCacheTable[i]);
	PATHCACHESIZE *= 2;
	pathCacheTable = null;

	pathCacheTable = new PathCacheElement[PATHCACHESIZE];
	for (Integer i = 0; i < PATHCACHESIZE/2; ++i)
	    pathCacheTable[i] = new PathCacheElement(Q[i]);
	for (Integer i = PATHCACHESIZE/2; i < PATHCACHESIZE; ++i) {
	    pathCacheTable[i] = new PathCacheElement();
	    pathCacheTable[i].setStateEmpty();
	}
    }

    void resetPathCacheTable() {
	PathCacheElement Q[] = new PathCacheElement[PATHCACHESIZE];
	for (Integer i = 0; i < PATHCACHESIZE; ++i)
	    Q[i] = new PathCacheElement(pathCacheTable[i]);
	pathCacheTable = null;
	PATHCACHESIZE *= 2;
	pathCacheTable = new PathCacheElement[PATHCACHESIZE];
	for (Integer i = 0; i < PATHCACHESIZE/2; ++i)
	    pathCacheTable[i] = new PathCacheElement(Q[i]);
	for (Integer i = PATHCACHESIZE/2; i < PATHCACHESIZE; ++i) {
	    pathCacheTable[i] = new PathCacheElement();
	    pathCacheTable[i].setStateEmpty();
	}
    }
    
    
    /*************** オペレーションキャッシュ ***************/

    protected enum Op { Change, Union, Intersection, Setminus };

    class OpCacheElement {
    	Op _op ;
    	int _F;
    	int _G;
    	int _H;
    	State _state;

	OpCacheElement() {
	    _op = null;
	    _F = _G = _H = -1;
	    _state = State.Empty;
	}
    	OpCacheElement(Op op,int f,int g,int h, State state){
    	    _op = op;
    	    _F = f;
    	    _G = g;
    	    _H = h;
    	    _state = state;
    	}
	OpCacheElement(OpCacheElement base) {
	    _op = base._op;
	    _F = base._F;
	    _G = base._G;
	    _H = base._H;
	    _state = base._state;
	}
	public Op getOp() { return _op; }
	public int getF() { return _F; }
	public int getG() { return _G; }
	public int getH() { return _H; }
	public State getState() { return _state; }
	public void setOp(Op op) { _op = op; }
	public void setF(int F) { _F = F; }
	public void setG(int G) { _G = G; }
	public void setH(int H) { _H = H; }
    	public void setState(State state) { _state = state; }
    	public void setStateEmpty() { _state = State.Empty; }
    }

    Integer OPCACHETABLESIZE;
    OpCacheElement[] OpCacheTable = null;

    public Integer hfOp(final Op o, final int f, final int g) {
    	int uo = 0;
    	if (Op.Change == o)
    	    uo = 1;
    	if (Op.Union == o)
    	    uo = 2;
    	if (Op.Intersection == o)
    	    uo = 3;
    	if (Op.Setminus == o)
    	    uo = 4;
    	return ((100*uo) + (10*f) + g) % OPCACHETABLESIZE;
    }

    public boolean eqOpTableEntry(final int k, final Op o, final int f, final int g) {
    	if (OpCacheTable[k].getOp() != o) 
	    return false;
    	if (OpCacheTable[k].getF() != f) 
	    return false;
    	if (OpCacheTable[k].getG() != g) 
	    return false;
    	return true;
    }
    
    public int memberOp(Op o, int f, int g) {
    	int i, k;
    	State cstate;

    	k = i = hfOp(o, f, g);
    	do {
    	    cstate = OpCacheTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (eqOpTableEntry(k, o, f, g)) 
		    return OpCacheTable[k].getH(); /* find the input node */
    	    }
    	    k = (k+1) % OPCACHETABLESIZE;
    	    /* printf("collision occurs in op\n"); */
    	} while (State.Empty != cstate && k != i);
    	return -2; /* there is no input node */
    }

    public void insertOp(final Op o, final int f, final int g, final int h) {
    	int i, k, found = -1;
    	State cstate;

    	k = i = hfOp(o, f, g);
    	do {
    	    cstate = OpCacheTable[k].getState();
    	    if (State.Empty == cstate || State.Deleted == cstate) {
		if (found < 0) /* there is an empty cell */
		    found = k;
	    } 
    	    else {
		if (eqOpTableEntry(k, o, f, g)) /* input node already exists */
		    return;
	    } 
    	    k = (k+1) % OPCACHETABLESIZE;
    	    /* printf("collision occurs in op\n"); */
    	} while (State.Empty != cstate && k != i);


    	OpCacheTable[found].setState(State.Occupied);
    	OpCacheTable[found].setOp(o);
    	OpCacheTable[found].setF(f);
    	OpCacheTable[found].setG(g);
    	OpCacheTable[found].setH(h);
    }

    public void deleteOp(final Op o, final int f, final int g) {
    	int i, k;
    	State cstate;

    	k = i = hfOp(o, f, g);
    	do {
    	    cstate = OpCacheTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (eqOpTableEntry(k, o, f, g)) { /* find the input node and delete it */
		    OpCacheTable[k].setState(State.Deleted); 
		    return; 
		} 
    	    }
    	    k = (k+1) % OPCACHETABLESIZE;
    	    /* printf("collision occurs in op\n"); */
    	} while (State.Empty != cstate && k != i);
    	return; /* there is no input node */
    }

    void setOpCacheTableEmpty() {
	for (Integer i = 0; i < OPCACHETABLESIZE; ++i)
	    OpCacheTable[i].setStateEmpty();
    }

    void resetOpCacheTable() {
	OpCacheTable = null;
	OPCACHETABLESIZE *= 2;
	OpCacheTable = new OpCacheElement[OPCACHETABLESIZE];
	for (Integer i = 0; i < OPCACHETABLESIZE; ++i) {
	    OpCacheTable[i] = new OpCacheElement();
	    OpCacheTable[i].setStateEmpty();
	}
    }

    void increaseOpCacheSlots() {
	OpCacheElement[] OP = new OpCacheElement[OPCACHETABLESIZE];
	for (Integer i = 0; i < OPCACHETABLESIZE; ++i)
	    OP[i] = new OpCacheElement(OpCacheTable[i]);
	OPCACHETABLESIZE *= 2;
	OpCacheTable = null;

	OpCacheTable = new OpCacheElement[OPCACHETABLESIZE];
	for (Integer i = 0; i < OPCACHETABLESIZE/2; ++i)
	    OpCacheTable[i] = new OpCacheElement(OP[i]);
	for (Integer i = OPCACHETABLESIZE/2; i < OPCACHETABLESIZE; ++i)
	    OpCacheTable[i].setStateEmpty();
    }
    void zddPrintSub(final int P, HashSet<Integer> U) {
	if (-1 == P)
	    return;
	if (U.contains(P))
	    return;
	System.out.println(P + " : " + zddTable[P]);
	U.add(P);
	zddPrintSub(zddTable[P].getLeft(), U);
	zddPrintSub(zddTable[P].getRight(), U);
    }
    void zddPrint(final int P) {
	if (-1 == P)
	    return;
	HashSet<Integer> U = new HashSet<Integer>();
	zddPrintSub(P, U);
    }
}
