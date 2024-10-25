
# EmaDB ORM

EmaDB is a lightweight Java ORM (Object-Relational Mapping) tool designed to simplify database interactions. It supports **PostgreSQL** and **MongoDB** (MongoDB support is currently in progress), offering a quick, minimalistic setup and operation. This project is created for educational purposes and serves as a foundation for further development. Contributions are welcome!

## Features
- **Multi-Database Support**: Currently supports PostgreSQL, with MongoDB support underway.
- **Simple Configuration**: Define your database connection details easily using annotations.
- **Dynamic Table Naming**: Table names are generated automatically or customized as needed.
- **Upsert Operations**: Insert or update records seamlessly based on entity annotations.
- **Educational MVP**: Ideal for learning and experimentation with ORM concepts.

## Installation

Clone this repository and import it into your Java project.

```bash
git clone https://github.com/AmirDevCoder/EmaDB.git
```

## Quick Start

1. **Configure Database Connection**  
   Define the configuration by creating a class that implements `EmaConfig` and annotates it with `@Config`.

   ```java
   @Config(db = DB.POSTGRESQL) // specify your DB type (POSTGRESQL, MONGODB)
   public class PostgresConfig implements EmaConfig {

       @Override
       public String getHost() { return "localhost"; }

       @Override
       public String getPort() { return "5432"; }

       @Override
       public String getDBName() { return "test"; }

       @Override
       public String getUsername() { return "emamagic"; }

       @Override
       public String getPassword() { return "1234"; }
   }

### Defining Entities and Table Naming

When defining an entity with `@Entity`, specifying a table name is optional. If a name is not provided, EmaDB automatically generates one by converting the class name to lowercase and pluralizing it (e.g., `User` becomes `users`). This way, you can avoid manual naming unless a specific table name is required.

#### Key Annotations

- **`@Entity`** - Marks the class as an entity to map to a database table.
- **`@Id`** - Specifies the primary key field. EmaDB currently supports only `Integer` for primary keys.
- **`@UniqueForUpdate`** - Marks a field as unique, allowing updates to target this field.
- **`@IgnoreRow`** - When applied to a field, this annotation ensures that the field is ignored during database mapping. This is helpful for fields used only within the Java project and not intended for storage in the database.

Each entity must also include a no-argument constructor for compatibility with EmaDB.

#### Example

```java
@Entity(db = DB.POSTGRESQL, name = "users")
public class User {
    @Id
    private Integer id; // Primary key (supports Integer only)
    private String name;
    
    @UniqueForUpdate
    private String email; // Unique field for updates

    @IgnoreRow
    private String sessionToken; // Used in application only, not stored in DB

    // No-argument constructor
    public User() {}
}
```

## Perform CRUD Operations

Use the following methods to perform basic CRUD operations.

```java
public class Main {
    public static void main(String[] args) {

        // Insert or update a user record
        Optional<User> upsertedUser = EmaDB.upsert(getUser());

        // Delete records
        boolean isDeleted = EmaDB.delete(getUser());

        // Read records
        Optional<List<User>> users = EmaDB.read(User.class);

        // Close the database connection
        EmaDB.close();

    }

    private static User getUser() {
        var user = new User();
        user.setName("ali");
        user.setEmail("ali@gmail.com");
        return user;
    }
}
```

## Considerations

- **MVP Status**: This is an early-stage project built primarily for learning, and it may have limitations and edge cases yet to be covered.
- **Contribution Friendly**: Feel free to contribute to enhance functionality, add MongoDB support, or refine features.
- **Note**: This project contains a lot of TODOs, and exceptions are not handled thoroughly.

