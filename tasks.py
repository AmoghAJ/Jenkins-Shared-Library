import boto3
from boto3.dynamodb.conditions import Key, Attr
from invoke import task

def table_obj():
    dynamodb = boto3.resource('dynamodb', region_name='eu-central-1')
    return dynamodb.Table('release_management')

def get_item_by_application_version(app_verion):
    table = table_obj()
    response = table.query(KeyConditionExpression=Key('application_version').eq(app_verion))
    items = response['Items']
    return items

@task
def application_version_exist(c, app_version):
    exist = "true" if get_item_by_application_version(app_version) else "false"
    print(exist)

def get_release_appver_date(app_verion):
    table = table_obj()
    response = table.get_item(Key={'application_version': app_verion})
    item = response['Item']
    return item

@task
def update_released(c, app_version):
    table = table_obj()
    resp = get_release_appver_date(app_version)
    resp['released'] = 'true'
    table.put_item(Item = resp)

@task
def get_pending_releases(c):
    table = table_obj()
    response = table.scan(
    FilterExpression=Attr('released').eq('false')
    )
    items = response['Items']
    print(items)

@task
def get_pending_releases_for_date(c, date):
    table = table_obj()
    response = table.scan(
    FilterExpression=Attr('released').eq('false') & Attr('release_date').eq(date)
    )
    items = response['Items']
    print(items)

@task
def insert_new_release_info(c, app_version, rel_date, artifact, rel_to_qa, rel_to_test, rel_to_prod):
    table = table_obj()
    table.put_item(Item = 
                   {'application_version': app_version,
                    'release_date': rel_date, 
                    'artifact_path': artifact, 
                    'release_to_qa': rel_to_qa,
                    'release_to_test': rel_to_test,
                    'release_to_prod': rel_to_prod,
                    'released': 'false'
                    })