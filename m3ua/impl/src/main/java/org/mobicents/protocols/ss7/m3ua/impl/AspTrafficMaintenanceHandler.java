package org.mobicents.protocols.ss7.m3ua.impl;

import javolution.util.FastList;

import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.m3ua.ExchangeType;
import org.mobicents.protocols.ss7.m3ua.Functionality;
import org.mobicents.protocols.ss7.m3ua.IPSPType;
import org.mobicents.protocols.ss7.m3ua.impl.fsm.FSM;
import org.mobicents.protocols.ss7.m3ua.impl.fsm.UnknownTransitionException;
import org.mobicents.protocols.ss7.m3ua.message.MessageClass;
import org.mobicents.protocols.ss7.m3ua.message.MessageType;
import org.mobicents.protocols.ss7.m3ua.message.asptm.ASPActive;
import org.mobicents.protocols.ss7.m3ua.message.asptm.ASPActiveAck;
import org.mobicents.protocols.ss7.m3ua.message.asptm.ASPInactive;
import org.mobicents.protocols.ss7.m3ua.message.asptm.ASPInactiveAck;
import org.mobicents.protocols.ss7.m3ua.parameter.ErrorCode;
import org.mobicents.protocols.ss7.m3ua.parameter.RoutingContext;
import org.mobicents.protocols.ss7.m3ua.parameter.TrafficModeType;

/**
 * 
 * @author amit bhayani
 * 
 */
public class AspTrafficMaintenanceHandler extends MessageHandler {

	private static final Logger logger = Logger.getLogger(AspTrafficMaintenanceHandler.class);

	public AspTrafficMaintenanceHandler(AspFactory aspFactory) {
		super(aspFactory);
	}

	private void handleAspInactive(Asp asp, ASPInactive aspInactive) {
		As appServer = asp.getAs();

		FSM aspPeerFSM = asp.getPeerFSM();
		if (aspPeerFSM == null) {
			logger.error(String.format("Received ASPINACTIVE=%s for ASP=%s. But peer FSM for ASP is null.",
					aspInactive, this.aspFactory.getName()));
			return;
		}

		FSM asLocalFSM = appServer.getLocalFSM();
		if (asLocalFSM == null) {
			logger.error(String.format("Received ASPINACTIVE=%s for ASP=%s. But local FSM for AS is null.",
					aspInactive, this.aspFactory.getName()));
			return;
		}

		ASPInactiveAck aspInactAck = (ASPInactiveAck) this.aspFactory.messageFactory.createMessage(
				MessageClass.ASP_TRAFFIC_MAINTENANCE, MessageType.ASP_INACTIVE_ACK);
		aspInactAck.setRoutingContext(appServer.getRoutingContext());

		this.aspFactory.write(aspInactAck);

		try {
			aspPeerFSM.setAttribute(FSM.ATTRIBUTE_MESSAGE, aspInactive);
			aspPeerFSM.signal(TransitionState.ASP_INACTIVE);

			// Signal AS to transition
			asLocalFSM.setAttribute(As.ATTRIBUTE_ASP, asp);
			asLocalFSM.signal(TransitionState.ASP_INACTIVE);

		} catch (UnknownTransitionException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void handleAspActive(Asp asp, ASPActive aspActive) {
		As appServer = asp.getAs();

		TrafficModeType trfModType = aspActive.getTrafficModeType();

		if (appServer.getTrafficModeType() != null) {
			// AppServer has Traffic Mode Type defined check if it
			// matches with sent ASP ACTIVE Message
			if (trfModType != null && appServer.getTrafficModeType().getMode() != trfModType.getMode()) {

				// Traffic Mode Type mismatch. Send Error.
				// TODO should send error or drop message?
				ErrorCode errorCodeObj = this.aspFactory.parameterFactory
						.createErrorCode(ErrorCode.Unsupported_Traffic_Mode_Type);
				this.sendError(appServer.getRoutingContext(), errorCodeObj);
				return;
			}

			// message doesn't have Traffic Mode Type
		} else {

			// AppServer Traffic Mode Type is optionally configured via
			// management config. If not select the first available in
			// AspUp message

			if (trfModType == null) {
				// Asp UP didn't specify the Traffic Mode either. use
				// default which is loadshare
				appServer.setDefaultTrafficModeType();
			} else {
				// Set the Traffic Mode Type passed in ASP ACTIVE
				appServer.setTrafficModeType(trfModType);
			}
		}

		FSM aspPeerFSM = asp.getPeerFSM();
		if (aspPeerFSM == null) {
			logger.error(String.format("Received ASPACTIVE=%s for ASP=%s. But peer FSM for ASP is null.", aspActive,
					this.aspFactory.getName()));
			return;
		}

		FSM asLocalFSM = appServer.getLocalFSM();
		if (asLocalFSM == null) {
			logger.error(String.format("Received ASPACTIVE=%s for ASP=%s. But local FSM for AS is null.", aspActive,
					this.aspFactory.getName()));
			return;
		}

		ASPActiveAck aspActAck = (ASPActiveAck) this.aspFactory.messageFactory.createMessage(
				MessageClass.ASP_TRAFFIC_MAINTENANCE, MessageType.ASP_ACTIVE_ACK);
		aspActAck.setTrafficModeType(appServer.getTrafficModeType());
		aspActAck.setRoutingContext(appServer.getRoutingContext());

		this.aspFactory.write(aspActAck);

		try {
			aspPeerFSM.setAttribute(FSM.ATTRIBUTE_MESSAGE, aspActive);
			aspPeerFSM.signal(TransitionState.ASP_ACTIVE);

			// Signal AS to transition
			asLocalFSM.setAttribute(As.ATTRIBUTE_ASP, asp);
			asLocalFSM.signal(TransitionState.ASP_ACTIVE);

		} catch (UnknownTransitionException e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected void handleAspActive(ASPActive aspActive) {

		RoutingContext rc = aspActive.getRoutingContext();

		if (aspFactory.getFunctionality() == Functionality.SGW
				|| (aspFactory.getFunctionality() == Functionality.AS && aspFactory.getExchangeType() == ExchangeType.DE)
				|| (aspFactory.getFunctionality() == Functionality.IPSP && aspFactory.getExchangeType() == ExchangeType.DE)
				|| (aspFactory.getFunctionality() == Functionality.IPSP
						&& aspFactory.getExchangeType() == ExchangeType.SE && aspFactory.getIpspType() == IPSPType.SERVER)) {
			if (rc == null) {
				Asp asp = this.getAspForNullRc();

				if (asp == null) {
					// Error condition
					logger.error(String
							.format("Rx : ASP ACTIVE=%s with null RC for Aspfactory=%s. But no ASP configured for null RC. Sent back Error",
									aspActive, this.aspFactory.getName()));
					return;
				}
				handleAspActive(asp, aspActive);
			} else {
				long[] rcs = rc.getRoutingContexts();

				for (int count = 0; count < rcs.length; count++) {
					Asp asp = this.aspFactory.getAsp(rcs[count]);

					if (asp == null) {
						// this is error. Send back error
						RoutingContext rcObj = this.aspFactory.parameterFactory
								.createRoutingContext(new long[] { rcs[count] });
						ErrorCode errorCodeObj = this.aspFactory.parameterFactory
								.createErrorCode(ErrorCode.Invalid_Routing_Context);
						sendError(rcObj, errorCodeObj);
						logger.error(String
								.format("Rx : ASPACTIVE=%s with RC=%d for Aspfactory=%s. But no ASP configured for this RC. Sending back Error",
										aspActive, rcs[count], this.aspFactory.getName()));
						continue;
					}
					handleAspActive(asp, aspActive);
				}// for
			}

		} else {
			// TODO : Should we silently drop ASPACTIVE?

			// ASPUP_ACK is unexpected in this state
			ErrorCode errorCodeObj = this.aspFactory.parameterFactory.createErrorCode(ErrorCode.Unexpected_Message);
			sendError(null, errorCodeObj);
		}
	}

	private void handleAspActiveAck(Asp asp, ASPActiveAck aspActiveAck, TrafficModeType trMode) {
		As as = asp.getAs();

		if (trMode == null) {
			trMode = asp.getAs().getDefaultTrafficModeType();
		}

		as.setTrafficModeType(trMode);

		FSM aspLocalFSM = asp.getLocalFSM();
		if (aspLocalFSM == null) {
			logger.error(String.format("Received ASPACTIVE_ACK=%s for ASP=%s. But local FSM is null.", aspActiveAck,
					this.aspFactory.getName()));
			return;
		}

		try {
			aspLocalFSM.signal(TransitionState.ASP_ACTIVE_ACK);

			if (aspFactory.getFunctionality() == Functionality.IPSP) {
				// If its IPSP, we know NTFY will not be received,
				// so transition AS FSM here
				FSM asPeerFSM = as.getPeerFSM();

				if (asPeerFSM == null) {
					logger.error(String.format("Received ASPACTIVE_ACK=%s for ASP=%s. But Peer FSM of AS=%s is null.",
							aspActiveAck, this.aspFactory.getName(), as));
					return;
				}

				asPeerFSM.setAttribute(As.ATTRIBUTE_ASP, asp);
				asPeerFSM.signal(TransitionState.AS_STATE_CHANGE_ACTIVE);
			}
		} catch (UnknownTransitionException e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected void handleAspActiveAck(ASPActiveAck aspActiveAck) {
		if (!this.aspFactory.started) {
			// If management stopped this ASP, ignore ASPActiveAck
			return;
		}

		RoutingContext rc = aspActiveAck.getRoutingContext();

		if (aspFactory.getFunctionality() == Functionality.AS
				|| (aspFactory.getFunctionality() == Functionality.SGW && aspFactory.getExchangeType() == ExchangeType.DE)
				|| (aspFactory.getFunctionality() == Functionality.IPSP && aspFactory.getExchangeType() == ExchangeType.DE)
				|| (aspFactory.getFunctionality() == Functionality.IPSP
						&& aspFactory.getExchangeType() == ExchangeType.SE && aspFactory.getIpspType() == IPSPType.CLIENT)) {

			TrafficModeType trMode = aspActiveAck.getTrafficModeType();

			if (rc == null) {
				Asp asp = this.getAspForNullRc();

				if (asp == null) {
					// Error condition
					logger.error(String
							.format("Rx : ASP ACTIVE_ACK=%s with null RC for Aspfactory=%s. But no ASP configured for null RC. Sent back Error",
									aspActiveAck, this.aspFactory.getName()));
					return;
				}
				handleAspActiveAck(asp, aspActiveAck, trMode);
			} else {
				long[] rcs = rc.getRoutingContexts();
				for (int count = 0; count < rcs.length; count++) {
					Asp asp = this.aspFactory.getAsp(rcs[count]);
					handleAspActiveAck(asp, aspActiveAck, trMode);
				}// for
			}

		} else {
			// TODO : Should we silently drop ASPACTIVE_ACK?

			// ASPACTIVE_ACK is unexpected in this state
			ErrorCode errorCodeObj = this.aspFactory.parameterFactory.createErrorCode(ErrorCode.Unexpected_Message);
			sendError(rc, errorCodeObj);
		}

	}

	protected void handleAspInactive(ASPInactive aspInactive) {
		RoutingContext rc = aspInactive.getRoutingContext();

		if (aspFactory.getFunctionality() == Functionality.SGW
				|| (aspFactory.getFunctionality() == Functionality.AS && aspFactory.getExchangeType() == ExchangeType.DE)
				|| (aspFactory.getFunctionality() == Functionality.IPSP && aspFactory.getExchangeType() == ExchangeType.DE)
				|| (aspFactory.getFunctionality() == Functionality.IPSP
						&& aspFactory.getExchangeType() == ExchangeType.SE && aspFactory.getIpspType() == IPSPType.SERVER)) {
			if (rc == null) {
				Asp asp = this.getAspForNullRc();

				if (asp == null) {
					// Error condition
					logger.error(String
							.format("Rx : ASPINACTIVE=%s with null RC for Aspfactory=%s. But no ASP configured for null RC. Sent back Error",
									aspInactive, this.aspFactory.getName()));
					return;
				}
				handleAspInactive(asp, aspInactive);
			} else {
				long[] rcs = rc.getRoutingContexts();

				for (int count = 0; count < rcs.length; count++) {
					Asp asp = this.aspFactory.getAsp(rcs[count]);

					if (asp == null) {
						// this is error. Send back error
						RoutingContext rcObj = this.aspFactory.parameterFactory
								.createRoutingContext(new long[] { rcs[count] });
						ErrorCode errorCodeObj = this.aspFactory.parameterFactory
								.createErrorCode(ErrorCode.Invalid_Routing_Context);
						sendError(rcObj, errorCodeObj);
						logger.error(String
								.format("Rx : ASPINACTIVE=%s with RC=%d for Aspfactory=%s. But no ASP configured for this RC. Sending back Error",
										aspInactive, rcs[count], this.aspFactory.getName()));
						continue;
					}
					handleAspInactive(asp, aspInactive);
				}// for
			}

		} else {
			// TODO : Should we silently drop ASPINACTIVE?

			// ASPUP_ACK is unexpected in this state
			ErrorCode errorCodeObj = this.aspFactory.parameterFactory.createErrorCode(ErrorCode.Unexpected_Message);
			sendError(null, errorCodeObj);
		}
	}

	protected void handleAspInactiveAck(ASPInactiveAck aspInactiveAck) {
		if (!this.aspFactory.started) {
			// If management stopped this ASP, ignore ASPInactiveAck
			return;
		}

		RoutingContext rc = aspInactiveAck.getRoutingContext();

		if (aspFactory.getFunctionality() == Functionality.AS
				|| (aspFactory.getFunctionality() == Functionality.SGW && aspFactory.getExchangeType() == ExchangeType.DE)
				|| (aspFactory.getFunctionality() == Functionality.IPSP && aspFactory.getExchangeType() == ExchangeType.DE)
				|| (aspFactory.getFunctionality() == Functionality.IPSP
						&& aspFactory.getExchangeType() == ExchangeType.SE && aspFactory.getIpspType() == IPSPType.CLIENT)) {

			if (rc == null) {
				Asp asp = this.getAspForNullRc();

				if (asp == null) {
					// Error condition
					logger.error(String
							.format("Rx : ASPINACTIVE_ACK=%s with null RC for Aspfactory=%s. But no ASP configured for null RC. Sent back Error",
									aspInactiveAck, this.aspFactory.getName()));
					return;
				}
				handleAspInactiveAck(asp, aspInactiveAck);
			} else {

				long[] rcs = aspInactiveAck.getRoutingContext().getRoutingContexts();
				for (int count = 0; count < rcs.length; count++) {
					Asp asp = this.aspFactory.getAsp(rcs[count]);

					if (asp == null) {
						// this is error. Send back error
						RoutingContext rcObj = this.aspFactory.parameterFactory
								.createRoutingContext(new long[] { rcs[count] });
						ErrorCode errorCodeObj = this.aspFactory.parameterFactory
								.createErrorCode(ErrorCode.Invalid_Routing_Context);
						sendError(rcObj, errorCodeObj);
						logger.error(String
								.format("Rx : ASPINACTIVE_ACK=%s with RC=%d for Aspfactory=%s. But no ASP configured for this RC. Sending back Error",
										aspInactiveAck, rcs[count], this.aspFactory.getName()));
						continue;
					}

					handleAspInactiveAck(asp, aspInactiveAck);
				}// for
			}

		} else {
			// TODO : Should we silently drop ASPINACTIVE_ACK?

			// ASPINACTIVE_ACK is unexpected in this state
			ErrorCode errorCodeObj = this.aspFactory.parameterFactory.createErrorCode(ErrorCode.Unexpected_Message);
			sendError(rc, errorCodeObj);
		}

	}

	private void handleAspInactiveAck(Asp asp, ASPInactiveAck aspInactiveAck) {
		FSM aspLocalFSM = asp.getLocalFSM();
		if (aspLocalFSM == null) {
			logger.error(String.format("Received ASPINACTIVE_ACK=%s for ASP=%s. But local FSM is null.",
					aspInactiveAck, this.aspFactory.getName()));
			return;
		}

		As as = asp.getAs();

		try {
			aspLocalFSM.signal(TransitionState.ASP_INACTIVE_ACK);

			if (this.aspFactory.getFunctionality() == Functionality.IPSP) {
				// If its IPSP, we know NTFY will not be received,
				// so transition AS FSM here
				FSM asPeerFSM = as.getPeerFSM();

				if (asPeerFSM == null) {
					logger.error(String.format(
							"Received ASPINACTIVE_ACK=%s for ASP=%s. But Peer FSM of AS=%s is null.", aspInactiveAck,
							this.aspFactory.getName(), as));
					return;
				}

				if (as.getTrafficModeType().getMode() == TrafficModeType.Loadshare) {
					// If it is loadshare and if there is atleast one other ASP
					// who ACTIVE, dont transition AS to INACTIVE
					for (FastList.Node<Asp> n = as.getAspList().head(), end = as.getAspList().tail(); (n = n.getNext()) != end;) {
						Asp remAspImpl = n.getValue();

						FSM aspPeerFSM = remAspImpl.getPeerFSM();
						AspState aspState = AspState.getState(aspPeerFSM.getState().getName());

						if (aspState == AspState.ACTIVE) {
							return;
						}
					}
				}
				
				//TODO : Check if other ASP are INACTIVE, if yes ACTIVATE them
				asPeerFSM.setAttribute(As.ATTRIBUTE_ASP, asp);
				asPeerFSM.signal(TransitionState.AS_STATE_CHANGE_PENDING);
			}
		} catch (UnknownTransitionException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
