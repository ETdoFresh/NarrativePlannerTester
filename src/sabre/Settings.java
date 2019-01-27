package sabre;

import java.nio.ByteBuffer;

public class Settings {

	public static final int MAJOR_VERSION_NUMBER = 0;
	public static final int MINOR_VERSION_NUMBER = 31;
	public static final long VERSION_UID = ByteBuffer.allocate(8).putInt(MAJOR_VERSION_NUMBER).putInt(MINOR_VERSION_NUMBER).getLong(0);
	public static final String VERSION_STRING = MAJOR_VERSION_NUMBER + "." + MINOR_VERSION_NUMBER;
	public static final String CREDITS = "Stephen G. Ware";
	
	public static final int UNIVERSAL_SUPERTYPE_ID = 0;
	public static final String UNIVERSAL_SUPERTYPE_NAME = "entity";
	public static final int BOOLEAN_TYPE_ID = 1;
	public static final String BOOLEAN_TYPE_NAME = "boolean";
	public static final int AGENT_TYPE_ID = 2;
	public static final String AGENT_TYPE_NAME = "agent";
	public static final int EMPTY_ENTITY_ID = 0;
	public static final String EMPTY_ENTITY_NAME = "Null";
	public static final int BOOLEAN_FALSE_ID = EMPTY_ENTITY_ID;
	public static final String BOOLEAN_FALSE_NAME = "False";
	public static final int BOOLEAN_TRUE_ID = 1;
	public static final String BOOLEAN_TRUE_NAME = "True";
	public static final int AUTHOR_AGENT_ID = 2;
	public static final String AUTHOR_AGENT_NAME = "Author";
	public static final String EXPRESSION_VARIABLE_NAME = "expression";
	public static final int INTENTIONAL_PROPERTY_ID = 0;
	public static final String INTENTIONAL_PROPERTY_NAME = "intends";
}
