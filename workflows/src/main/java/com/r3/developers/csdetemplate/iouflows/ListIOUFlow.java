package com.r3.developers.csdetemplate.iouflows;

import com.r3.developers.csdetemplate.utxoexample.states.IOUState;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.flows.RPCRequestData;
import net.corda.v5.application.flows.RPCStartableFlow;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
public class ListIOUFlow implements RPCStartableFlow {

    private final static Logger log = LoggerFactory.getLogger(ListIOUFlow.class);

    @CordaInject
    public JsonMarshallingService jsonMarshallingService;

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    public UtxoLedgerService utxoLedgerService;

    @Suspendable
    @Override
    public String call(RPCRequestData requestBody) {

        log.info("ListIOUFlow.call() called");

        // Queries the VNode's vault for unconsumed states and converts the result to a serializable DTO.
        List<StateAndRef<IOUState>> states = utxoLedgerService.findUnconsumedStatesByType(IOUState.class);
        List<ListIOUFlowArgs> results = states.stream().map(stateAndRef ->
                new ListIOUFlowArgs(
                        stateAndRef.getState().getContractState().getId(),
                        stateAndRef.getState().getContractState().getLender().toString(),
                        stateAndRef.getState().getContractState().getBorrower().toString(),
                        stateAndRef.getState().getContractState().getAmount(),
                        stateAndRef.getState().getContractState().getAmountPaid()
                )
        ).collect(Collectors.toList());

        // Uses the JsonMarshallingService's format() function to serialize the DTO to Json.
        return jsonMarshallingService.format(results);
    }
}

/*
RequestBody for triggering the flow via http-rpc:
{
    "clientRequestId": "list-1",
    "flowClassName": "com.r3.developers.csdetemplate.utxoexample.workflows.ListChatsFlow",
    "requestData": {}
}
*/