package servidordns;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.io.ByteArrayInputStream;

public class ServidorDNS {
    private final static int PORT = 53;

    public static void main(String[] args){
        boolean up = true; //Estado del servidor (Encendido o Apagado)
        byte[] requestBytes = new byte[1024]; //Almacenar los bytes recibidos
        byte[] responseBytes = new byte[1024]; //Almacenar los bytes de respuesta
        
        try {
            addresses = FileHandler.readFromFile(file);
        } catch (IOException e){
            e.printStackTrace();
        }
        
        System.out.println("Servidor DNS UP");
        try{
            DatagramSocket socket = new DatagramSocket(PORT);
            
            while(up){
                DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length);
                
                socket.receive(packet);
                System.out.println("Se recibio correctamente la peticion.");
                
                String domain = new String (packet.getData());
                System.out.println(domain);
                
                String resolvedAddress = addresses.get(domain);
                
                if(resolvedAddress != null){
                    System.out.println("Peticion del cliente: " + domain + " / Direccion IP : " + resolvedAddress);
                }else{
                    try{
                        InetAddress address_search = java.net.InetAddress.getByName(domain);
                        resolvedAddress = address_search.getHostAddress();
                        
                        System.out.println("Peticion del cliente: " + domain + " / Direccion IP : " + resolvedAddress);
                        
                        addresses.put(domain, resolvedAddress);
                        
                        fileHandler.appendToFile(file, domain, resolvedAddress);
                    }
                    catch(UnknownHostException ex){
                        System.out.println("Peticion del cliente: " + domain + " / Direccion IP : No encontrada.");
                    }
                }
                responseBytes = createResponse(resolvedAddress, requestBytes);
                DatagramPacket send_packet = new DatagramPacket(responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                socket.send(send_packet);
            }
            FileHandler.writeToFile(file, addresses);
            socket.close();
            
        } catch(SocketException e){
            System.out.println("No se pudo conextar al puerto");
            
        } catch(IOException e){
            System.out.println("Error en la recepcion del mensaje");
        }
            
     
    }
}
