create user prueba with password 'pruebabasededatos';
create database prueba1;
\connect prueba1
grant all privileges on database prueba1 to prueba;

create table personal (
	persona_uuid  uuid primary key,
	dni,
        apellido VARCHAR(20),
        nombre  VARCHAR(20)
);

grant select, insert, update, delete on all tables in schema public to prueba;

public interface Model{
	UUID createPersona(integer dni, VARCHAR(20) apellido, VARCHAR(20) nombre);
	List getAllPersona();
	boolean existPersona(UUID persona);
}

@Data
public class persona{
	private uuid persona_uuid;
	private integer dni;
	private VARCHAR(20) apellido;
	private VARCHAR(20) nombre;
}

public class sql2oModel implements Model{
	private Sql2o sql2o;
	private UuidGnerator uuidGenerator;

	public Sql2oModel(Sql2o sql2o){
		this.sql2o = sql2o;
		uuidGenerator = new RandomUuidGenerator();	
	}
	@Override
	public UUID createPersona (integer dni, VARCHAR(20) apellido, VARCHAR(20) nombre){
		try (Connection conn = sql2o.beginTransaction()){
			UUID personaUuid = uuidGenerator.generate();
			conn.createQuery("insert into persona(persona_uuid,dni,apellido,nombre) 				VALUES (:persona_uuid, :dni, :apellido, :nombre)")
				.addParameter("persona_uuid",personaUuid)
				.addParameter("dni",dni)
				.addParameter("apellido",apellido)
				.addParameter("nombre",nombre)
				.executeUpdate();
			return personaUuid;	
		}
	}
	
	@Override
	public List<persona> getAllPersona(){
		try (Connection conn = Sql2o.Open()){
			List<Persona> persona = conn.createQuery("select * from 	                persona").executeAndFetch(Persona.class);
			//persona.forEach((persona)->persona.set)		
		}
	}

	@Override
 	public boolean existPersona(UUID persona){
		try(Connection conn = Sql2o.open()){
			List<Persona> = conn.createQuery("select * from persona where   persona_uuid=:persona")
			.addParameter("persona",persona)
			.executeAndFetch(Persona.class);
		return persona.size() > 0;
		}
	}
}


public static void main (String[] args){
	CommandLineOptions options = new CommandLineOptions();
    	new JCommander(options, args);

    	logger.finest("Options.debug = " + options.debug);
    	logger.finest("Options.database = " + options.database);
    	logger.finest("Options.dbHost = " + options.dbHost);
    	logger.finest("Options.dbUsername = " + options.dbUsername);
    	logger.finest("Options.dbPort = " + options.dbPort);
    	logger.finest("Options.servicePort = " + options.servicePort);

    	port(options.servicePort);

    Sql2o sql2o = new Sql2o("jdbc:mysql://" + options.dbHost + ":" + options.dbPort + "/" + options.database,
            options.dbUsername, options.dbPassword, new MysqlQuirks() {
        {
            // make sure we use default UUID converter.
            converters.put(UUID.class, new UUIDConverter());
        }
    });

    Model model = new Sql2oModel(sql2o); 
    // insert a post (using HTTP post method)
    post("/posts", (request, response) -> {
        ObjectMapper mapper = new ObjectMapper();
        NewPostPayload creation = mapper.readValue(request.body(), NewPostPayload.class);
        if (!creation.isValid()) {
            response.status(HTTP_BAD_REQUEST);
            return "";
        }
        UUID id = model.createPersona(creation.getdni(), creation.getApellido(), creation.getNombre());
        response.status(200);
        response.type("application/json");
        return id;
    });

    // get all post (using HTTP get method)
    get("/persona", (request, response) -> {
        response.status(200);
        response.type("application/json");
        return dataToJson(model.getAllPersona());
    });

    persona("/persona/:uuid", (request, response) -> {
        ObjectMapper mapper = new ObjectMapper();
        NewCommentPayload creation = mapper.readValue(request.body(), NewCommentPayload.class);
        if (!creation.isValid()) {
            response.status(HTTP_BAD_REQUEST);
            return "";
        }
        UUID persona = UUID.fromString(request.params(":uuid"));
        if (!model.existPersona(persona)){
            response.status(400);
            return "";
        }
       // UUID id = model.createComment(post, creation.getAuthor(), creation.getContent());
       // response.status(200);
       // response.type("application/json");
       // return id;
    });

    get("/persona/:uuid", (request, response) -> {
        UUID persona = UUID.fromString(request.params(":uuid"));
        if (!model.existPersona(post)) {
            response.status(400);
            return "";
        }
        response.status(200);
        response.type("application/json");
        //return dataToJson(model.getAllCommentsOn(post));
    });
}
