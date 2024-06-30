import { useState, useEffect } from 'react';
import { useHistory } from "react-router-dom";
import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { Formik, Field } from 'formik';
import * as yup from 'yup';
import { Link } from 'react-router-dom';



const container = {
	backgroundColor: '#FFFFFF', padding: '2rem', marginTop: '9rem',
	boxShadow: '4px 4px 4px lightgray', borderRadius: '6px'
}

const coluna = {
	backgroundColor: '#FFFFFF', margin: '0.5rem', alignItems: 'center'
}

const botao = {
	backgroundColor: '#5E3797', border: '0', marginLeft: '1rem', width: '80%', fontSize: '1.5rem'
}

export default function Inicial() {
	return (
		<>
			<Container style={container}>
				<Row className="justify-content-md-center" style={{ padding: '1rem' }}>
					<h1 className="display-6"> Qual o tipo de conta você gostaria de cadastrar? </h1>
				</Row>
				<Row className="justify-content-md-center" style={{ paddingBottom: '1rem', paddingTop: '1rem' }}>
					<Col md style={coluna}>
						<Link to="/physical">
							<Button style={botao}> Conta Física </Button>
						</Link>
					</Col>
					<Col md style={coluna}>
						<Link to="/juridical">
							<Button style={botao}> Conta Jurídica </Button>
						</Link>
					</Col>
					<Col md style={coluna}>
						<Link to="/joint">
							<Button style={botao}> Conta Conjunta </Button>
						</Link>
					</Col>
				</Row>
			</Container>
		</>
	);
}