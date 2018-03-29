package gr.dkateros.springboot.backendtest.control;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import gr.dkateros.springboot.backendtest.entity.DnsRecord;

@Transactional
public interface DnsRecordRepository 
extends JpaRepository<DnsRecord, Long> {
	//EMPTY
}
