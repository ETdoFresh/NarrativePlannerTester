package qa;

public class AgentSchemaPair {
	public String agent;
	public String schema;
	
	public AgentSchemaPair(String agent, String schema) {
		this.agent = agent;
		this.schema = schema;
	}
	
	@Override
	public String toString() {
		return "[" + agent + ", " + schema + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentSchemaPair other = (AgentSchemaPair) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!schema.equals(other.schema))
			return false;
		return true;
	}	

}
