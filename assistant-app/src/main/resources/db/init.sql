
create table if not exists users
(
	id bigint generated always as identity,
	name varchar(100),
	surname varchar(100),
	patronymic varchar(100),
	full_name varchar(400), --generated always as (name || ' '  || surname || ' '  || patronymic) stored
	email varchar(100),
	is_active boolean default true,
	created_at timestamp default now(),
	primary key (id)
);

create table if not exists competency
(
    id bigint generated always as identity,
    name varchar(100),
	level varchar(100),
	priority integer,
	test_time_minutes integer,
	pass_threshold integer,
	unique(name, priority, level),
	primary key(id)
);

create table if not exists skill
(
	id bigint generated always as identity,
	competency_id bigint,
	name varchar(100),
	file_name varchar(200),
	foreign key (competency_id) references competency(id)
		on delete cascade
		on update cascade,
	primary key(id)
);

create table if not exists user_competency
(
    id bigint generated always as identity,
	user_id bigint,
	competency_id bigint,
	completed boolean,
	foreign key (user_id) references users(id)
		on delete cascade
		on update cascade,
	foreign key (competency_id) references competency(id)
		on delete cascade
		on update cascade,
	primary key (id)
);

create table if not exists test_attempt
(
    id bigint generated always as identity,
	user_competency_id bigint,
	solution_duration integer,
	uploaded_at timestamp without time zone,
	foreign key (user_competency_id) references user_competency(id)
		on delete cascade
		on update cascade,
	primary key (id)
);

--create table if not exists markdown
--(
--    id bigint generated always as identity,
--    file_id varchar(200),
--    skill_id bigint,
--    foreign key (skill_id) references skill(id)
--    	on delete cascade
--    	on update cascade,
--    primary key(id)
--);

create table if not exists test_question
(
    id bigint generated always as identity,
    competency_id bigint,
    question_description varchar(500),
    foreign key (competency_id) references competency(id)
    	on delete cascade
    	on update cascade,
    primary key(id)
);

create table if not exists answer_option
(
    id bigint generated always as identity,
    tq_id bigint,
    option varchar(500),
    is_correct boolean,
    foreign key (tq_id) references test_question(id)
    	on delete cascade
    	on update cascade,
    primary key(id)
);

create table if not exists user_answers
(
    id bigint generated always as identity,
	test_attempt_id bigint,
	ao_id bigint,
	foreign key (test_attempt_id) references test_attempt(id)
		on delete cascade
		on update cascade,
	foreign key (ao_id) references answer_option(id)
		on delete cascade
		on update cascade,
	primary key (id)
);

insert into users(
	name, surname, patronymic, email)
	values ('name1', 'surname1', 'patronymic1', 'email1'),
	       ('name2', 'surname2', 'patronymic2', 'email2'),
	       ('name3', 'surname3', 'patronymic3', 'email3');


insert into competency(name, level, priority)
	values ('analytics', 'basic', 1),
           ('analytics', 'advanced', 2),
           ('analytics', 'pro', 3),
           ('java', 'basic', 1),
           ('java', 'advanced', 2),
           ('java', 'pro', 3),
           ('python', 'basic', 1),
           ('python', 'advanced', 2),
           ('python', 'pro', 3);

insert into skill(competency_id, name)
	values (1, 'skill1'),
           (1, 'skill2'),
           (4, 'skill3'),
           (4, 'skill1'),
           (4, 'skill2'),
           (7, 'skill3'),
           (7, 'skill1'),
           (7, 'skill2'),
           (7, 'skill3');

insert into user_competency(user_id, competency_id, completed)
	values (1, 1, false),
           (1, 2, false),
           (2, 1, false),
           (2, 2, false),
           (3, 1, false),
           (3, 2, false);

insert into test_question(competency_id, question_description)
    values (1, 'how to 1'),
           (1, 'how to 2'),
           (2, 'how to 3'),
           (2, 'how to 4'),
           (3, 'how to 5'),
           (3, 'how to 6');

insert into answer_option(tq_id, option, is_correct)
    values (1, 'answer1', false),
           (1, 'answer2', true),
           (3, 'answer3', false),
           (3, 'answer4', true),
           (5, 'answer5', false),
           (5, 'answer6', true);