# Java-DNS-Server
Servidor DNS funcional desarrollado en Java.

## Información Importante
Este proyecto esta basado en el [**RFC 1035 "DOMAIN NAMES - IMPLEMENTATION AND SPECIFICATION"**](https://www.ietf.org/rfc/rfc1035.txt) publicado por la IETF.
En el siguiente código solo se van a manejar dominios **QTYPE = A (0000000000000001) y QCLASS = IN (0000000000000001)** para las peticiones DNS.
Se sigue una arquitectura Cliente/Servidor.

## Utilización
El archivo ***MasterFiles.txt*** es el componente en el cual se guarda la información de los dominios, con su respectiva dirección IP para poder ser traducida. Al ejecutar el código, se esperan a peticiones por parte del cliente para la resolución de nombres de dominios gracias a este archivo de texto.

##  Documentación
El siguiente [documento](Java-DNS-Server/Documentacion Servidor DNS - Grupo 2.pdf) ha sido diseñado para proporcionar toda la información necesaria con respecto a los conceptos utilizados en el desarrollo y explicación del código.
