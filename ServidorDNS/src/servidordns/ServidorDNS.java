//*****************************************************************
//Nicolas David Rincon Ballesteros
//Octubre 2022
//Servidor DNS funcional desarrollado en Java.
//Observaciones solo se van a manejar dominios QTYPE = A (0000000000000001) y QCLASS = IN (0000000000000001) para las peticiones DNS.
//*****************************************************************

package servidordns;

import java.io.File; //Poder trabajar con archivos
import java.io.BufferedWriter; //Para escribir archivos
import java.io.FileNotFoundException;
import java.io.FileWriter; //Poder escribir archivos
import java.io.IOException; //Para manejar errores, especialmente con archivos
import java.util.Scanner; //Para leer archivo
import java.net.DatagramPacket; //Para trabajar con Datagramas
import java.net.DatagramSocket; //Para trabajar con el socket para recibir datagramas
import java.net.InetAddress; //Para buscar IP con DNS de la Javeriana

//Solo se van a manejar dominios QTYPE = A (0000000000000001) y QCLASS = IN (0000000000000001)

public class ServidorDNS {
    
    public static void main(String[] args) throws IOException {
        
        byte[] packet = new byte[512]; //Se crea packet para recibir
        int qname_l; //Determinar la longitud de la question (nombre de dominio)
        byte[] qname;
        DatagramSocket socket = new DatagramSocket(53,InetAddress.getLocalHost()); //Se crea socket con el puerto 53 para recibir el packet
        DatagramPacket request = new DatagramPacket(packet, 512); //Se guarda el packet REQUEST con el packet y su longitud en bytes
        
 
        while(true){ //Mientras el servidor este encendido (Programa este corriendo)
            socket.receive(request); //Recibe la peticion DNS del cliente
            
            qname_l = request.getLength()-17; //Obtiene la longitud del contenido exceptuando QTYPE y QCLASS
            qname = new byte[qname_l]; //Crear arreglo para guardar URL
            
            //Empezar a leer el packet
            //Esto de aqui hacia abajo es para decodificar la URL
            for(int i=12; i<request.getLength()-5; i++){
                qname[i-12] = packet[i];
            }

            int cont = 0; //Contador para saber donde vamos
            int length = qname[cont]+1; //Almacenar la longitud de la URL
            
            //Este ciclo while es para cambiar los numeros a '.' y se pone el limite para que acabe al final
            while(cont+length<qname.length-1){
                cont += length; //Actualizar contador
                length = qname[cont]+1; //La posicion qname[contador]+ tiene la cantidad de caracteres de un fragmento de la url
                qname[cont] = '.'; //Reemplazar por el punto (3www9javeriana3edu2co = www.javeriana.edu.co)
            }
            
            String url = new String(qname); //Esto traduce de bytes a caracteres legibles
            url = url.trim(); //Esto para quitar los espacios antes y despues
            
            String ip = leerDocumento(url); //Buscar el nombre de dominio en el archivo MasterFiles
            //Si no lo encuentra, retorna 0, si lo encuentra retorna la direccion ip
            
            if (!"0".equals(ip)){ //Si lo que retorna es distinto de 0, quiere decir que si encontro el nombre de dominio, imprime la direccion ip
                //No fue necesario buscar en el DNS de la javeriana el nombre de dominio
            }
            else{ //En el caso que no lo encuentre en el .txt, recurre a internet para buscar la direccion IP (DNS JAVERIANA)
                InetAddress direccionIP = InetAddress.getByName(url); //Devuelve la direccion IP con su nombre de dominio, dado un nombre de dominio, lo busca en internet
                escribirDocumento(url, direccionIP.getHostAddress()); 
                ip = direccionIP.getHostAddress(); //.getHostAddress retorna la direccion IP
            }
            
            imprimirTraduccion(url, ip);
            
            //Empezar a hacer el packet de respuesta
            byte[] response_packet = new byte[512];
            
            Integer x = 128;
            Integer y = 0;
            Integer z = 1;

            System.arraycopy(packet, 0, response_packet, 0, request.getLength());//Copia arreglo que llego a el packet de respuesta
            //Esto para simplemente cambiar algunos valores, muchos de estos se mantienen igual, Ej. ID.

            //ID se queda igual
            //response_packet[0]
            //response_packet[1]  

            //QR Cambiar de 0 -> 1 (Esto porque pasa de query (0) a response(1))
            response_packet[2] = x.byteValue(); // 10000000 = 128
            response_packet[3] = y.byteValue(); // 00000000 = 0

            //QDCOUNT se queda igual (Porque este RR es respuesta, no pregunta)
            //response_packet[4]
            //response_packet[5]

            //ANCOUNT cambia de 0 -> 1 porque hay una respuesta (una IP)
            response_packet[6] = y.byteValue(); // 00000000 = 0
            response_packet[7] = z.byteValue(); // 00000001 = 1 (Una respuesta del servidor)

            //NSCOUNT dejarlo en 0 porque dice que no esta asignado, no usamos authority
            response_packet[8] = y.byteValue(); // 00000000 = 0
            response_packet[9] = y.byteValue(); // 00000000 = 0

            //ARCOUNT se queda igual, no se usan RR's adicionales
            //response_packet[10]
            //response_packet[11]

            //El QTYPE (A) se debe poner en 1
            response_packet[request.getLength()-4] = y.byteValue(); // 00000000 = 0 FINAL MENOS 4
            response_packet[request.getLength()-3] = z.byteValue(); // 00000001 = 1 FINAL MENOS 3

            //El QCLASS (IN) se debe poner en 1
            response_packet[request.getLength()-2] = y.byteValue(); // 00000000 = 0 FINAL MENOS 2
            response_packet[request.getLength()-1] = z.byteValue(); // 00000001 = 1 FINAL MENOS 1

            //Guardar respuesta en packet para ser enviado
            if(!ip.equals("0")){ //Si se encuentra la direccion
                //QNAME es la URL que se habia encontrado antes
                int cont_r = 12; //Puntero para saber donde estamos (Header son 12 bytes)
                for(int i=request.getLength();i<request.getLength()+qname_l+1;i++){
                    response_packet[i]=packet[cont_r];
                    cont_r ++;
                }
                cont_r = request.getLength()+qname_l+1; //Esto para apuntar al final

                //TYPE, tipo A = 1 (0000000000000001)
                response_packet[cont_r] = y.byteValue(); // 00000000 = 0
                cont_r++;
                response_packet[cont_r] = z.byteValue(); // 00000001 = 1
                cont_r++;

                //CLASS, clase IN = 1 (0000000000000001)
                response_packet[cont_r] = y.byteValue(); // 00000000 = 0
                cont_r++;
                response_packet[cont_r] = z.byteValue(); // 00000001 = 1
                cont_r++;

                //TTL, generalmente el valor por defecto es 43200 (12 horas), segun Google
                //TTL son 4 bytes, los primeros 2 son 0
                //43200 en binario es 1010100011000000
                //Se divide en dos bytes = 10101000 (168) y 11000000 (192)
                Integer ttl_1 = 168;
                Integer ttl_2 = 192;
                response_packet[cont_r] = y.byteValue(); // 00000000 = 0
                cont_r++;
                response_packet[cont_r] = y.byteValue(); // 00000000 = 0
                cont_r++;
                response_packet[cont_r] = ttl_1.byteValue(); // 10101000 (168)
                cont_r++;
                response_packet[cont_r] = ttl_2.byteValue(); // 11000000 (192)
                cont_r++;

                //Revisar
                //RDLENGTH 
                response_packet[cont_r] = y.byteValue(); // 00000000 = 0
                cont_r++;

                //RDATA
                y = 4; //Se menciona que si es A y IN, generalmente esta parte tiene un valor de 4.
                response_packet[cont_r] = y.byteValue();
                cont_r++;

                //Direccion IP
                //System.out.println("IP: " + ip);
                String[] ip_sec = ip.split("\\.");
                Integer parte_ip;

                for(String str:ip_sec){                
                    parte_ip = Integer.valueOf(str);
                    response_packet[cont_r] = parte_ip.byteValue();
                    cont_r++;
                }
            }
            else { //Si no se encuentra la direccion IP, pone error
                y = 0;

                response_packet[6] = y.byteValue();
                response_packet[7] = y.byteValue();

                y = 3;
                response_packet[3] = y.byteValue();
            }
            
            //Enviar packet
            DatagramPacket sendpacket = new DatagramPacket(response_packet, response_packet.length, request.getAddress(), request.getPort());
            socket.send(sendpacket);
        }
    }
    
    //Funciones Importantes
    //Leer Master File
    public static String leerDocumento(String url) throws IOException{
        try {
            File text = new File("MasterFiles.txt"); // abre el archivo maestro
            Scanner scnr = new Scanner(text); //Scanner
            // while loop to iterate
            if(scnr.hasNextLine()){
                scnr.nextLine();
                while (scnr.hasNextLine()){
                    Scanner sc=new Scanner(scnr.nextLine());
                    if(url.equals(sc.next())){
                        sc.next();
                        sc.next();
                        return sc.next(); //Si se encuentra en el archivo
                    }
                }
                return "0"; //Si no esta en el archivo
            }
            else{
                return "Archivo Vacio"; //Si el archivo esta vacio
            }  
        }
        catch(FileNotFoundException e) {
            return "Error"; //Si hay un error
        }       
    }

    //Escribir Master File
    public static void escribirDocumento(String direccion, String ip) throws IOException{
        String textoI = "\n" + direccion + " IN A " + ip; //Formato que se usara en el MasterFile
        try(FileWriter myWriter = new FileWriter("MasterFiles.txt", true);
        BufferedWriter escribir = new BufferedWriter(myWriter);){
            escribir.write(textoI);
        }
    }
    
    
    //Imprimir traduccion de la peticion
    public static void imprimirTraduccion(String url, String ip){
        //Imprime en consola la informacion del packet que recibe
        System.out.println("-");
        System.out.println("Nombre de Dominio recibido: " + url);
        System.out.println("Direccion IP encontrada: " + ip);
        System.out.println("-\n");
    }  
}

    



