package gr.dkateros.springboot.backendtest.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import gr.dkateros.springboot.backendtest.entity.validation.Hostname;
import gr.dkateros.springboot.backendtest.entity.validation.IPv4Address;

@Entity
public class DnsRecord {
	
	@Id
	@GeneratedValue
	Long id;
	
	@IPv4Address
	@NotNull
	String ipAddress;
	
	@Hostname
	@NotNull
	String hostName;
	
	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

}
