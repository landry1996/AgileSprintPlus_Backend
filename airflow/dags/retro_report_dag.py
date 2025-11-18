from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.bash import BashOperator

default_args={"owner":"agile-sprint-plus","retries":1,"retry_delay":timedelta(minutes=5)}
with DAG(dag_id="weekly_retro_report",start_date=datetime(2025,1,1),schedule_interval="0 18 * * FRI",catchup=False,default_args=default_args,tags=["retro","agile"]) as dag:
    export_data=BashOperator(task_id="export_data",bash_command="echo 'Export sprint data'")
    build_report=BashOperator(task_id="build_report",bash_command="echo 'Generate Markdown retro'")
    notify=BashOperator(task_id="notify",bash_command="echo 'Email retro to team'")
    export_data>>build_report>>notify
