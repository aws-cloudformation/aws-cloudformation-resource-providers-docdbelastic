# AWS::DocDBElastic::Cluster

The AWS::DocDBElastic::Cluster Amazon DocumentDB (with MongoDB compatibility) Elastic Scale resource describes a Cluster

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::DocDBElastic::Cluster",
    "Properties" : {
        "<a href="#clustername" title="ClusterName">ClusterName</a>" : <i>String</i>,
        "<a href="#adminusername" title="AdminUserName">AdminUserName</a>" : <i>String</i>,
        "<a href="#adminuserpassword" title="AdminUserPassword">AdminUserPassword</a>" : <i>String</i>,
        "<a href="#shardcapacity" title="ShardCapacity">ShardCapacity</a>" : <i>Integer</i>,
        "<a href="#shardcount" title="ShardCount">ShardCount</a>" : <i>Integer</i>,
        "<a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>" : <i>[ String, ... ]</i>,
        "<a href="#subnetids" title="SubnetIds">SubnetIds</a>" : <i>[ String, ... ]</i>,
        "<a href="#preferredmaintenancewindow" title="PreferredMaintenanceWindow">PreferredMaintenanceWindow</a>" : <i>String</i>,
        "<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#authtype" title="AuthType">AuthType</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::DocDBElastic::Cluster
Properties:
    <a href="#clustername" title="ClusterName">ClusterName</a>: <i>String</i>
    <a href="#adminusername" title="AdminUserName">AdminUserName</a>: <i>String</i>
    <a href="#adminuserpassword" title="AdminUserPassword">AdminUserPassword</a>: <i>String</i>
    <a href="#shardcapacity" title="ShardCapacity">ShardCapacity</a>: <i>Integer</i>
    <a href="#shardcount" title="ShardCount">ShardCount</a>: <i>Integer</i>
    <a href="#vpcsecuritygroupids" title="VpcSecurityGroupIds">VpcSecurityGroupIds</a>: <i>
      - String</i>
    <a href="#subnetids" title="SubnetIds">SubnetIds</a>: <i>
      - String</i>
    <a href="#preferredmaintenancewindow" title="PreferredMaintenanceWindow">PreferredMaintenanceWindow</a>: <i>String</i>
    <a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#authtype" title="AuthType">AuthType</a>: <i>String</i>
</pre>

## Properties

#### ClusterName

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>50</code>

_Pattern_: <code>[a-zA-z][a-zA-Z0-9]*(-[a-zA-Z0-9]+)*</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AdminUserName

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### AdminUserPassword

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ShardCapacity

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ShardCount

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VpcSecurityGroupIds

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SubnetIds

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PreferredMaintenanceWindow

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### KmsKeyId

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AuthType

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ClusterArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### ClusterArn

Returns the <code>ClusterArn</code> value.
