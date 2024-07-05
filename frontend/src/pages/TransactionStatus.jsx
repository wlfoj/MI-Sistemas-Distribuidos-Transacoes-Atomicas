import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Container, Alert, Row, Col, Button } from 'react-bootstrap';
import Api from '../utils/api'; 

const container = {
    backgroundColor: '#FFFFFF', padding: '2rem', marginTop: '1.5rem', marginBottom: '1.5rem',
    boxShadow: '4px 4px 4px lightgray', borderRadius: '6px', width: '60%'
}

const TransactionStatus = () => {
    const { tid } = useParams();
    const [transactionData, setTransactionData] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (tid) {
            Api.get(`/user/transaction/${tid}`)
                .then(response => {
                    setTransactionData(JSON.stringify(response.data, null, 4));
                })
                .catch(err => {
                    setError(err.message || 'Erro ao obter o status da transação');
                });
        }
    }, [tid]);

    return (
        <Container style={container}>
            <Row>
                <Col style={{ textAlign: 'right' }}>
                    <Link to="/home">
                        <Button style={{ backgroundColor: '#5E3797', border: '0' }}>
                            Home
                        </Button>
                    </Link>
                </Col>
            </Row>
            {error && <Alert variant="danger">{error}</Alert>}
            {transactionData && <pre>{transactionData}</pre>}
        </Container>
    );
};

export default TransactionStatus;
