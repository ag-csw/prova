package ws.prova.reference2;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import ws.prova.kernel2.ProvaConstant;
import ws.prova.kernel2.ProvaObject;
import ws.prova.kernel2.ProvaUnification;
import ws.prova.kernel2.ProvaVariable;
import ws.prova.kernel2.ProvaVariablePtr;

public class ProvaVariableImpl extends ProvaTermImpl implements ProvaVariable {

	private static final long serialVersionUID = 7501612596168443208L;

	private Object name;
	
	private Class<?> type;
	
	// The term that this variable is assigned to
	private ProvaObject assigned;

	// Where this variable is in the rule's variables
	private int index;
	
	private long ruleId;

	private static AtomicLong incName = new AtomicLong(0);
	
	public static ProvaVariableImpl create() {
		return new ProvaVariableImpl();
	}

	public static ProvaVariableImpl create(String name) {
		return new ProvaVariableImpl(name);
	}

	public static ProvaVariableImpl create(String name, Class<?> type) {
		return new ProvaVariableImpl(name, type);
	}

	public static ProvaVariableImpl create(String name, ProvaObject assigned) {
		return new ProvaVariableImpl(name, assigned);
	}

	private ProvaVariableImpl() {
		this.name = incName.incrementAndGet();
		this.type = Object.class;
		this.assigned = null;
		this.index = -1;
	}
	
	private ProvaVariableImpl(String name) {
		this.name = "_".equals(name) ? incName.incrementAndGet() : name;
		this.type = Object.class;
		this.assigned = null;
		this.index = -1;
	}

	private ProvaVariableImpl( String name, Class<?> type ) {
		this.name = name;
		this.type = type;
		this.assigned = null;
		this.index = -1;
	}
	
	private ProvaVariableImpl(Class<?> type, ProvaObject assigned, int index,
			long ruleId) {
		this.name = incName.incrementAndGet();
		this.type = type;
		this.assigned = assigned;
		this.index = index;
		this.ruleId = ruleId;
	}

	private ProvaVariableImpl(String name, ProvaObject assigned) {
		this.name = name;
		this.assigned = assigned;
		this.type = Object.class;
		this.index = -1;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Object getName() {
		return name;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public void setAssigned(ProvaObject assigned) {
		this.assigned = assigned;
	}

	@Override
	public ProvaObject getAssigned() {
		return assigned;
	}
	
	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public ProvaObject getRecursivelyAssigned() {
		if( assigned==this ) {
			assigned=null;
			return this;
		}
		if( assigned==null )
			return this;
		ProvaObject recursivelyAssigned = assigned.getRecursivelyAssigned();
		if( assigned!=recursivelyAssigned )
			assigned = recursivelyAssigned;
		return recursivelyAssigned;
	}

	@Override
	public int collectVariables(long ruleId, List<ProvaVariable> variables) {
		if( assigned!=null ) {
			assigned.collectVariables(ruleId, variables);
			return -1;
		}
		int foundIndex = variables.indexOf(this);
		if( foundIndex!=-1 ) {
			index = foundIndex;
			return index;
		}
		index = variables.size();
		variables.add(this);
		return index;
	}

	@Override
	public int computeSize() {
		if( assigned!=null )
			return assigned.computeSize();
		return -1;
	}

//	@Override
//	public void collectVariables(long ruleId, Vector<ProvaVariable> variables, int offset) {
//		collectVariables(ruleId, variables);
//	}
	
	@Override
	public ProvaVariable clone() {
		ProvaVariableImpl newVariable = new ProvaVariableImpl(type,assigned,index,ruleId);
		return newVariable;
	}

	@Override
	public ProvaVariable clone(long ruleId) {
		ProvaVariableImpl newVariable = new ProvaVariableImpl(type,assigned,index,ruleId);
		return newVariable;
	}

	@Override
	public boolean unify(ProvaObject target, ProvaUnification unification) {
		if( target==null ) {
			assigned = ProvaListImpl.emptyRList;
			return true;
		}
		if( target instanceof ProvaVariable ) {
			final Class<?> targetType = ((ProvaVariable) target).getType();
			if( targetType.isAssignableFrom(type) ) {
				((ProvaVariable) target).setAssigned(this);
				return true;
			}
			if( type.isAssignableFrom(targetType) ) {
				assigned = target;
				return true;
			}
			return false;
//			if( !((ProvaVariable) target).getType().isAssignableFrom(type) )
//				return false;
//			((ProvaVariable) target).setAssigned(this);
//			return true;
		}
		if( type!=Object.class ) {
			if( target instanceof ProvaConstant ) {
				if( !type.isInstance(((ProvaConstant) target).getObject()) )
					return false;
			}
		}
		assigned = target;
		return true;
	}

	@Override
	public void setRuleId(long ruleId) {
		this.ruleId = ruleId;
	}

	@Override
	public long getRuleId() {
		return ruleId;
	}

	public String toString() {
		if( assigned==null ) {
			StringBuilder sb = new StringBuilder();
			if( type!=Object.class ) {
				sb.append(type.getCanonicalName());
				sb.append('.');
			}
			String strName = name.toString();
			if( strName.length()!=0 && Character.isDigit(strName.charAt(0)) ) {
				sb.append("<");
				sb.append(name);
				sb.append('>');
			} else
				sb.append(name);
			return sb.toString();
		}
		return getRecursivelyAssigned().toString();
	}

	@Override
	public void substituteVariables(ProvaVariablePtr[] varsMap) {
	}

	@Override
	public boolean equals( Object o ) {
		ProvaVariableImpl var = (ProvaVariableImpl) o;
		return var.name.equals(name) && var.type==type;
	}

	@Override
	public boolean isGround() {
		return false;
	}

	@Override
	public String toString(List<ProvaVariable> variables) {
		return toString();
	}

	@Override
	public ProvaObject cloneWithVariables(List<ProvaVariable> variables) {
		if( assigned!=null )
			return assigned;
		return this;
	}

	@Override
	public ProvaObject cloneWithVariables(long ruleId, List<ProvaVariable> variables) {
		if( assigned!=null )
			return assigned;
		return this;
	}

	@Override
	public Object computeIfExpression() {
		return this;
	}

	@Override
	public Object compute() {
		return this;
	}

}
