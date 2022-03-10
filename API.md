## Adding Plasmo Emotes to the project

### Maven

```xml
<project>
    <repositories>
        <repository>
            <id>plasmo-repo</id>
            <url>https://repo.plo.su</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>su.plo</groupId>
            <artifactId>emotes</artifactId>
            <version>1.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

### Groovy DSL

```groovy
repositories {
    maven { url 'https://repo.plo.su/' }
}

dependencies {
    compileOnly 'su.plo:emotes:1.0.0'
}
```

### Kotlin DSL

```
repositories {
    maven {
        url = uri("https://repo.plo.su")
    }
}

dependencies {
    compileOnly("su.plo:emotes:1.0.0")
}
```

## Obtaining an instance of the API

### Using the Bukkit ServicesManager

```java
RegisteredServiceProvider<EmotesAPI> provider = Bukkit.getServicesManager().getRegistration(EmotesAPI.class);
if(provider != null) {
    EmotesAPI api = provider.getProvider();
}
```

## Methods

See
this: [EmotesAPI](https://github.com/plasmoapp/plasmo-emotes/blob/main/src/main/java/su/plo/emotes/api/EmotesAPI.java)
