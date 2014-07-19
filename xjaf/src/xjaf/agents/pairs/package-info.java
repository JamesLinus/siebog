/**
 * Implementation of a Sender-Receiver performance evaluation. The case-study consists 
 * of pairs of agents, named Sender and Receiver. Upon receiving a message from Sender, 
 * the Receiver agent performs a CPU-intensive task and replies with the result. Sender 
 * than calculates the message round-trip time (RTT) as the performance measure. The RTT
 * represents the time elapsed since the Sender has issued its original request, and until
 * the reply is received.
 * 
 * The number of pairs is gradually increased and the agents are distributed accross a 
 * cluster in order to measure how these factor affect the runtime performance of XJAF.
 */
package xjaf.agents.pairs;