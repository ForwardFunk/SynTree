package ast_utils;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ASTDeserializer implements JsonDeserializer<ASTNode> {

	@Override
	public ASTNode deserialize(JsonElement element, Type type,
			JsonDeserializationContext arg2) throws JsonParseException {
		// TODO Auto-generated method stub
		return null;
	}

}
