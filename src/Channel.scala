package iconara.amqp


import java.util.{Map => JavaMap, HashMap => JavaHashMap}
import java.lang.{Object => JavaObject}

import com.rabbitmq.client.{
  Channel => RMQChannel, 
  AMQP, 
  MessageProperties, 
  Consumer, 
  ShutdownSignalException, 
  Envelope,
  ConnectionFactory
}

import Utils.transformArguments


class Channel(channel: RMQChannel) {
  private val validExchangeTypes = Set('direct, 'fanout, 'topic)
  
  def createExchange(
    name: String, 
    exchangeType: Symbol = 'direct,
    durable: Boolean = false,
    autoDelete: Boolean = false,
    arguments: Map[String, Any] = Map.empty
  ): Exchange = {
    if (! (validExchangeTypes contains exchangeType)) {
      throw new IllegalArgumentException("\"%s\" is not a valid exchange type".format(exchangeType.name))
    }
    channel.exchangeDeclare(name, exchangeType.name, durable, autoDelete, transformArguments(arguments))
    getExchange(name)
  }
  
  def getExchange(name: String): Exchange = new Exchange(name, channel)
  
  def deleteExchange(name: String) {
    channel.exchangeDelete(name)
  }
  
  def createQueue(
    name: String,
    durable: Boolean = false,
    exclusive: Boolean = false,
    autoDelete: Boolean = false,
    arguments: Map[String, Any] = Map.empty
  ): Queue = {
    channel.queueDeclare(name, durable, exclusive, autoDelete, transformArguments(arguments))
    getQueue(name)
  }
  
  def getQueue(name: String): Queue = new Queue(name, channel)
  
  def createQueue(): Queue = {
    val name = channel.queueDeclare().getQueue()
    new Queue(name, channel)
  }
  
  def recover(requeue: Boolean = true) {
    channel.basicRecoverAsync(requeue)
  }
}

private object Utils {
  def transformArguments(inArgs: Map[String, Any]): JavaMap[String, JavaObject] = {
    inArgs.foldLeft(new JavaHashMap[String, JavaObject]) { case (m, (key, value)) => 
      m.put(key, value.asInstanceOf[JavaObject])
      m 
    }
  }
  
  val emptyArguments = transformArguments(Map.empty)
}