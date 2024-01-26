# Servidor Proxy en Java

Este proyecto implementa un servidor proxy en Java que actúa como intermediario entre los clientes y los servidores de destino. El servidor proxy permite ocultar la dirección IP real del cliente y manejar solicitudes HTTP y HTTPS.

Un proxy es un servidor que actúa como un intermediario entre los usuarios y los servidores a los que desean acceder en la red. En lugar de que los usuarios se conecten directamente a un servidor, sus solicitudes primero pasan por el proxy, que luego reenvía esas solicitudes al servidor correspondiente. Del mismo modo, las respuestas del servidor pasan a través del proxy antes de llegar de nuevo al usuario. Esto crea una capa adicional de separación entre el cliente y el servidor.

## Funcionamiento de un Proxy
El funcionamiento básico de un proxy implica los siguientes pasos:

1. **Solicitud del cliente:** Cuando un usuario desea acceder a un recurso en línea, como una página web, envía una solicitud al proxy en lugar de enviarla directamente al servidor de destino.

2. **Reenvío al servidor:** El proxy recibe la solicitud del cliente y, en función de su configuración, reenvía la solicitud al servidor de destino. El servidor de destino puede ser una página web, un servicio en línea o cualquier otro recurso.

3. **Solicitud al servidor y respuesta:** El servidor de destino procesa la solicitud y envía la respuesta al proxy. El proxy, a su vez, envía la respuesta al cliente que realizó la solicitud original.

4. **Respuesta al cliente:** El cliente recibe la respuesta del proxy, como si hubiera obtenido la respuesta directamente del servidor. El cliente generalmente no está consciente de la intermediación del proxy.

## Configuración y Ejecución

1. Clona el repositorio o copia el código fuente en tu sistema.

2. Abre el proyecto en un entorno de desarrollo compatible con Java, como Eclipse o IntelliJ IDEA.

3. Compila y ejecuta el archivo `Proxy_Server.java` para iniciar el servidor proxy en el puerto local especificado (por defecto, puerto 8080).

## Estructura del Proyecto

El proyecto consta de un solo archivo fuente:

- `Proxy_Server.java`: Este archivo contiene la implementación completa del servidor proxy, incluyendo métodos para manejar solicitudes HTTP y HTTPS, y para reenviar datos entre clientes y servidores de destino.

## Flujo del Código

El código del servidor proxy sigue el siguiente flujo:

1. **Configuración y Punto de Entrada:**
    - Define el puerto local en el que el servidor proxy escuchará.
    - Inicia el servidor proxy en el método `main`.

2. **Iniciando el Servidor Proxy:**
    - El método `Run_Server` inicia un bucle infinito para aceptar conexiones entrantes de los clientes.
    - Utiliza un `ServerSocket` para escuchar en el puerto local.
    - Utiliza un `ExecutorService` para manejar múltiples conexiones en hilos separados.

3. **Manejo de Conexiones Entrantes:**
    - El método `handleConnection` maneja cada conexión entrante.
    - Lee la solicitud inicial del cliente para determinar si es HTTP o HTTPS.
    - Llama a métodos para manejar la solicitud correspondiente.

4. **Manejo de Solicitudes HTTP:**
    - El método `handleHttpRequest` analiza la solicitud HTTP, extrae información del host de destino y reenvía la solicitud al servidor.
    - Lee la respuesta del servidor y la reenvía al cliente.

5. **Manejo de Solicitudes HTTPS:**
    - El método `handleHttpsRequest` maneja las solicitudes HTTPS (CONNECT).
    - Envía una respuesta exitosa al cliente y establece conexiones con el servidor de destino.
    - Crea hilos para reenviar datos entre cliente y servidor.

6. **Reenvío de Datos Entre Sockets:**
    - El método `forwardData` reenvía datos entre sockets de entrada y salida.
    - Utiliza un búfer para manejar bloques de datos.

7. **Gestión de Recursos y Errores:**
    - Utiliza `try-with-resources` para garantizar que los recursos se cierren correctamente.
    - Maneja errores y excepciones para evitar fugas de recursos.

8. **Consideraciones de Seguridad:**
    - Advertencia sobre la falta de implementaciones avanzadas de seguridad y recomendación de mejorarlas.

## Uso en un Entorno de Producción

- Para un uso en un entorno de producción real, se recomienda implementar medidas de seguridad, autenticación y filtrado de contenido para proteger el servidor proxy y los clientes.

- También, considera el uso de bibliotecas de seguridad y protocolos de encriptación adecuados para manejar conexiones HTTPS de manera segura.

## Conclusiones

Este proyecto demuestra cómo implementar un servidor proxy básico en Java para ocultar la dirección IP del cliente y manejar solicitudes HTTP y HTTPS. Sin embargo, para un uso en un entorno real, es esencial implementar medidas de seguridad más avanzadas y considerar las consideraciones de concurrencia y rendimiento.
